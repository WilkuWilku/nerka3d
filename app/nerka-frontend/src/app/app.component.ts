import {Component, OnInit} from '@angular/core';
import {
  AxesHelper, BufferGeometry,
  Color,
  GridHelper,
  PerspectiveCamera, Points, PointsMaterial,
  Scene, Vector3,
  WebGLRenderer
} from "three";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";
import {TriangleService} from "./triangle.service";
import {KidneyLayerData} from "./dto/kidneyLayerData";
import {KidneyDataLoaderService} from "./dataLoader/kidney-data-loader.service";
import {BackendDataLoaderService} from "./dataLoader/backend-data-loader.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  jsonData: any;
  scene: Scene;
  camera: PerspectiveCamera;
  renderer: WebGLRenderer;
  pointsCount = 0;
  controls: OrbitControls;
  layersTranslationData: Map<number, Array<number>> = new Map(); // Map<layerNumber, [X, Y, Z]>

  pointsReductionRatio: number = 1;
  distanceBetweenLayers: number = 200;
  loadedKidneyLayers: KidneyLayerData[] = [];

  constructor(private triangleService: TriangleService,
              private kidneyDataLoaderService: KidneyDataLoaderService,
              private backendDataLoaderService: BackendDataLoaderService) {
  }


  ngOnInit(): void {
    this.renderer = new WebGLRenderer();
    this.renderer.setSize(window.innerWidth * 0.75, window.innerHeight * 0.75);
    document.body.getElementsByClassName("three-container")[0].appendChild(this.renderer.domElement);

    this.camera = new PerspectiveCamera(45, window.innerWidth / window.innerHeight, 1, 30000);
    this.camera.position.set(-1000, 1500, -1000);

    this.controls = new OrbitControls( this.camera, this.renderer.domElement );
    this.controls.target.set(3000, -1000, 3000);
    this.controls.update();

    this.scene = new Scene();
    this.scene.background = new Color(0xf2f2f2);

    this.refreshScene();
    this.renderer.render( this.scene, this.camera );

    this.animate();


    // test pobierania danych z backendu
    this.backendDataLoaderService.getTestLayers().subscribe(response => {
      let pointsCoords: Vector3[] = [];
      response.forEach(layer => {
        layer.points.map(point => {
          pointsCoords.push(new Vector3(point.x, point.height, point.y));
        })
      })
      const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
      const material = new PointsMaterial({color: 0x8d34ff, size: 10});
      const points = new Points(pointsGeometry, material)
      this.scene.add(points);
    })
  }

  animate() {
    requestAnimationFrame( () => this.animate() );
    this.controls.update();
    this.renderer.render( this.scene, this.camera );
  }

  onFileUpload(event) {
    //todo: zarządzanie pamięcią - po ponownym załadowaniu tych samych plików, dane z poprzednich wciąż są zaalokowane
    this.loadedKidneyLayers = [];
    this.kidneyDataLoaderService.loadKidneyData(event.target.files, this.pointsReductionRatio, this.distanceBetweenLayers)
      .subscribe(
        loadedDataPart => {
          if(loadedDataPart instanceof KidneyLayerData) {
            console.log("Loaded part is KidneyLayerData")
            loadedDataPart.rawLayer.data = null; // dla oszczędności RAMu
            this.loadedKidneyLayers.push(loadedDataPart)
          } else if(loadedDataPart instanceof Map) {
            console.log("Loaded part is Map")
            this.layersTranslationData = loadedDataPart;
          }
        },
        errorMsg => console.error(errorMsg),
        () => {
          this.translateLayers();
          this.refreshScene()
        }
      )
  }

  onLayerVisibilityChanged() {
    this.refreshScene();
  }

  refreshScene() {
    this.scene.clear();
    const axesHelper = new AxesHelper(10);
    const gridHelper = new GridHelper(5000, 10);
    gridHelper.translateX(2500);
    gridHelper.translateZ(2500);
    this.scene.add(axesHelper, gridHelper);

    this.loadedKidneyLayers.forEach(layerData => {
      if(layerData.isVisible) {
        this.scene.add(layerData.layerPoints)
      }
    });
  }

  translateLayers() {
    this.layersTranslationData.forEach(([x,y,z], key) => {
      this.loadedKidneyLayers.filter(layer => parseInt(layer.rawLayer.header.layerNumber) == key + 1 )
        .forEach(layer => {
          console.log("translation", layer.rawLayer.header)
          // if(x && y && z) {
            // layer.layerPoints = layer.layerPoints
              // .translateX(-y)
              // .translateY(z) // z <-> y, bo dla threejs "y" to wysokość
              // .translateZ(-x)
          // }
        })

    })
  }

}

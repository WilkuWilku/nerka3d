import {Component, OnInit} from '@angular/core';
import {AxesHelper, Color, GridHelper, PerspectiveCamera, Scene, WebGLRenderer} from "three";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";
import {TriangleService} from "./triangle.service";
import {KidneyLayerData} from "./dto/kidneyLayerData";
import {KidneyDataLoaderService} from "./dataLoader/kidney-data-loader.service";

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

  pointsReductionRatio: number = 1;
  distanceBetweenLayers: number = 180;
  loadedKidneyLayers: KidneyLayerData[] = [];

  constructor(private triangleService: TriangleService,
              private kidneyDataLoaderService: KidneyDataLoaderService) {
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
        loadedLayer => this.loadedKidneyLayers.push(loadedLayer),
        errorMsg => console.error(errorMsg),
        () => this.refreshScene()
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

  setLayersOrder() {
    const orderedLayers = this.loadedKidneyLayers.sort((layer1, layer2) => {
        return parseInt(layer1.rawLayer.header.layerNumber) - parseInt(layer2.rawLayer.header.layerNumber)
      }
    )
    orderedLayers.forEach((layer, index) => {
      layer.layerHeight = index * this.distanceBetweenLayers;
    })
    this.loadedKidneyLayers = orderedLayers;
  }

}

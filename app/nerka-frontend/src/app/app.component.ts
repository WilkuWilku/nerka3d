import {Component, OnInit} from '@angular/core';
import {
  AxesHelper,
  BufferGeometry,
  Color,
  Face3,
  Geometry,
  GridHelper,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera, PointLight,
  Points,
  PointsMaterial,
  Scene,
  Vector3,
  WebGLRenderer
} from "three";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";
import {KidneyLayerData} from "./dto/kidneyLayerData";
import {KidneyDataLoaderService} from "./dataLoader/kidney-data-loader.service";
import {BackendDataLoaderService} from "./dataLoader/backend-data-loader.service";
import {Triangle} from "./dto/triangle";
import {Layer} from "./dto/layer";
import {Parameters} from "./dto/parameters";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  scene: Scene;
  camera: PerspectiveCamera;
  renderer: WebGLRenderer;
  controls: OrbitControls;
  light: PointLight;

  parameters: Parameters;
  displayMode = 'kidney';

  files;

  buttonEnabled = false;
  isProcessing = false;

  constructor(private kidneyDataLoaderService: KidneyDataLoaderService,
              private backendDataLoaderService: BackendDataLoaderService) {
  }


  ngOnInit(): void {
    this.renderer = new WebGLRenderer();
    this.renderer.setSize(window.innerWidth - 300, window.innerHeight - 16);
    document.body.getElementsByClassName("three-container")[0].appendChild(this.renderer.domElement);

    this.camera = new PerspectiveCamera(45, window.innerWidth / window.innerHeight, 1, 30000);
    this.camera.position.set(-1000, 1500, -1000);

    this.controls = new OrbitControls( this.camera, this.renderer.domElement );
    this.controls.target.set(3000, -1000, 3000);
    this.controls.update();

    this.scene = new Scene();
    this.scene.background = new Color(0xf2f2f2);

    this.refreshScene();

    this.light = new PointLight();
    this.light.intensity = 1;
    this.light.decay = 2;
    this.camera.add(this.light);

    this.renderer.render( this.scene, this.camera );

    this.animate();

    this.initParameters();

    // this.backendDataLoaderService.getTestLayers().subscribe(response => {
    //   this.drawLayersPoints(response);
    // })
    //
    // this.backendDataLoaderService.getTestTriangles().subscribe(response => {
    //   this.drawTriangles(response);
    // })

  }

  initParameters() {
    this.parameters = {
      displayedPointsPercent: 3,
      numberOfPointsOnLayer: 20,
      interpolationMethod: 'Linear',
      numberOfIntermediateLayers: 1,
      triangulationMethod: 'BY_POINTS_DISTANCE',
      pointsDeterminationMethod: 'EVENLY_DISTRIBUTED_EQUINUMEROUS',
      indexesRatioDiffCoefficient: 0
    }
  }

  animate() {
    requestAnimationFrame( () => this.animate() );
    this.controls.update();
    this.renderer.render( this.scene, this.camera );
  }

  onFileUpload(event) {
    this.files = event.target.files;
  }

  getFileUploadStatusText() {
    if (this.files && this.files.length > 0) {
      this.buttonEnabled = true;
      return 'Wgrano ' + this.files.length + " plików."
    } else {
      this.buttonEnabled = false;
      return 'Nie wgrano plików.';
    }
  }

  onVisualiseButtonClick() {
    if (this.displayMode === 'kidney') {
      this.isProcessing = true;
      this.backendDataLoaderService.getTrianglesFromFiles(this.files, this.parameters).subscribe(response => {
        this.refreshScene();
        this.drawTriangles(response);
        this.isProcessing = false;
      });
    } else if (this.displayMode === 'layer') {
      this.isProcessing = true;
      this.backendDataLoaderService.getLayersFromFiles(this.files, this.parameters).subscribe(response => {
        this.refreshScene();
        this.drawLayersPoints(response);
        this.isProcessing = false;
      });
    }
  }

  refreshScene() {
    this.scene.clear();
    const axesHelper = new AxesHelper(10);
    const gridHelper = new GridHelper(5000, 10);
    gridHelper.translateX(2500);
    gridHelper.translateZ(2500);
    this.scene.add(axesHelper, gridHelper);
    this.scene.add(this.camera)
  }

  drawTriangles(triangles: Triangle[]) {
    let geometry = new Geometry();
    triangles.forEach((triangle, index) => {
      const v1 = new Vector3(triangle.vertex1.x, triangle.vertex1.height, triangle.vertex1.y)
      const v2 = new Vector3(triangle.vertex2.x, triangle.vertex2.height, triangle.vertex2.y)
      const v3 = new Vector3(triangle.vertex3.x, triangle.vertex3.height, triangle.vertex3.y)
      const face = new Face3(3*index, 3*index+1, 3*index+2)
      geometry.vertices.push(v3, v2, v1);
      geometry.faces.push(face);
    })
    geometry.computeFaceNormals();
    const mesh = new Mesh(geometry, new MeshStandardMaterial({color: 0x0000ff}))
    this.scene.add(mesh);

  }

  drawLayersPoints(layers: Layer[]) {
    let pointsCoords: Vector3[] = [];
    layers.forEach(layer => {
      layer.points.map(point => {
        pointsCoords.push(new Vector3(point.x, point.height, point.y));
      })
    })
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    const material = new PointsMaterial({color: 0x8d34ff, size: 10});
    const points = new Points(pointsGeometry, material)
    this.scene.add(points);
  }

}

import {Component, OnInit} from '@angular/core';
import {
  AxesHelper,
  BufferGeometry,
  Color,
  GridHelper, Line, LineBasicMaterial, Mesh, MeshBasicMaterial,
  PerspectiveCamera,
  Points, PointsMaterial,
  Scene,
  Vector3,
  WebGLRenderer
} from "three";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";
import {TriangleService} from "./triangle.service";
import {DataLoader} from './dataLoader/dataLoader';
import {CtlReaderService} from "./dataLoader/ctl-reader.service";

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
  points: Points;
  pointsCount = 0;
  controls: OrbitControls;

  constructor(private triangleService: TriangleService, private ctlReaderService: CtlReaderService) {
  }


  ngOnInit(): void {
    this.renderer = new WebGLRenderer();
    this.renderer.setSize(window.innerWidth * 0.75, window.innerHeight * 0.75);
    document.body.appendChild(this.renderer.domElement);

    this.camera = new PerspectiveCamera(45, window.innerWidth / window.innerHeight, 1, 30000);
    this.camera.position.set(-1000, 1500, -1000);

    this.controls = new OrbitControls( this.camera, this.renderer.domElement );
    this.controls.target.set(3000, -1000, 3000);
    this.controls.update();

    this.scene = new Scene();
    this.scene.background = new Color(0xf2f2f2);

    const axesHelper = new AxesHelper(10);

    const gridHelper = new GridHelper(5000, 10);
    gridHelper.translateX(2500);
    gridHelper.translateZ(2500);

    this.scene.add(axesHelper, gridHelper);
    this.renderer.render( this.scene, this.camera );

    this.points = this.triangleService.createMockLayeredPoints();
    this.scene.add(this.points);


    this.animate();
  }

  animate() {
    requestAnimationFrame( () => this.animate() );
    this.controls.update();
    this.renderer.render( this.scene, this.camera );
  }

  onFileUpload(event) {
    this.scene.remove(this.points);
    //TODO: make density and layer spacing configurable by user
    // [this.points, this.pointsCount] = DataLoader.convertJsonToKidneyLayers(event, 5, 150);
    // this.scene.add(this.points);
    [this.points, this.pointsCount] = this.ctlReaderService.convertCtlFileToPoints(event.target.files)
    this.scene.add(this.points);
  }
}

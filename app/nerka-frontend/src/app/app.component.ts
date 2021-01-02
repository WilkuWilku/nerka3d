import {Component, OnInit} from '@angular/core';
import {
  AxesHelper,
  BufferGeometry,
  Color,
  GridHelper,
  PerspectiveCamera,
  Points, PointsMaterial,
  Scene,
  Vector3,
  WebGLRenderer
} from "three";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";

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
  pointsCount: number = 0;
  controls: OrbitControls;

  ngOnInit(): void {
    this.renderer = new WebGLRenderer();
    this.renderer.setSize(window.innerWidth*0.75, window.innerHeight*0.75);
    document.body.appendChild(this.renderer.domElement);

    this.camera = new PerspectiveCamera(45, window.innerWidth / window.innerHeight, 1, 10000);
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

    this.animate();
  }

  animate() {
    requestAnimationFrame( () => this.animate() );
    this.controls.update();
    this.renderer.render( this.scene, this.camera );
  }

  onFileUpload(event) {
    let selectedFile = event.target.files[0];
    const fileReader = new FileReader();
    fileReader.readAsText(selectedFile);
    fileReader.onload = () => {
      this.jsonData = JSON.parse(fileReader.result.toString());
      console.log(this.jsonData);
      this.scene.remove(this.points)
      this.points = this.createPointsFromJsonData();
      this.scene.add(this.points);
    }
  }

  createPointsFromJsonData(): Points {
    const y = 10;
    const pointsCoords = [];
    for(let i = 0; i<this.jsonData.length; i++) {
      for(let j = 0; j<this.jsonData[i].length; j++) {
        if(this.jsonData[i][j] == 1) {
          // y = height of layer
          pointsCoords.push(new Vector3(i,y,j));
        }
      }
    }
    this.pointsCount = pointsCoords.length;
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    return new Points(pointsGeometry, new PointsMaterial({color: 0x0000ff, size: 10}));
  }
}

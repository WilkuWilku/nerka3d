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
import {LayerPoint} from "./dto/layerPoint";

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
  loadedTriangles: Triangle[] = [];

  loadedKidneyLayers: KidneyLayerData[] = [];

  constructor(private kidneyDataLoaderService: KidneyDataLoaderService,
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

    this.backendDataLoaderService.getTestBorder().subscribe(response => {
      this.drawPoints(response, 0x522222, 1);
    })

    setTimeout(() => this.drawTestPoint(), 5000);

    // this.backendDataLoaderService.getTestLayers().subscribe(response => {
    //   this.drawLayersPoints(response, 0x46ff12, 35);
    // })

    // this.backendDataLoaderService.getTestSpline().subscribe(response => {
    //   this.drawPoints(response, 0xff8762, 35);
    // })
    //
    // this.backendDataLoaderService.getTestTriangles().subscribe(response => {
    //   this.drawTriangles(response);
    // })

  }

  animate() {
    requestAnimationFrame( () => this.animate() );
    this.controls.update();
    this.renderer.render( this.scene, this.camera );
  }

  onFileUpload(event) {

    this.refreshScene();
    // this.kidneyDataLoaderService.loadKidneyData(event.target.files, 0, 200).subscribe(data => {
    //   const kidneyData = data as KidneyLayerData;
    //   console.log(kidneyData)
    //   this.scene.add(kidneyData.layerPoints);
    // });

    // this.backendDataLoaderService.getBordersFromFiles(event.target.files).subscribe(response => {
    //   this.drawPoints(response, 0x0000ff, 4);
    // })


    this.backendDataLoaderService.getTrianglesFromFiles(event.target.files).subscribe(response => {
      this.loadedTriangles = response;
      this.drawTriangles(response);
    });
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

    const light = new PointLight();
    light.intensity = 0.5;
    light.decay = 2;
    this.camera.add(light)
    this.scene.add(this.camera)

    // this.loadedKidneyLayers.forEach(layerData => {
    //   if(layerData.isVisible) {
    //     this.scene.add(layerData.layerPoints)
    //   }
    // });

    // this.drawTestPoints();
    // this.drawTestSplinePoints();
    // this.backendDataLoaderService.getTestLayers().subscribe(response => {
    //   this.drawLayersPoints(response, 0x46ff12, 35);
    // })

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

  drawLayersPoints(layers: Layer[], color: number, size: number) {
    let pointsCoords: Vector3[] = [];
    layers.forEach(layer => {
      layer.points.map(point => {
        pointsCoords.push(new Vector3(point.x, point.height, point.y));
      })
    })
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    const material = new PointsMaterial({color: color, size: size});
    const points = new Points(pointsGeometry, material)
    this.scene.add(points);
  }

  drawPoints(inputPoints: LayerPoint[], color: number, size: number) {
    let pointsCoords: Vector3[] = [];

    inputPoints.map(point => {
      pointsCoords.push(new Vector3(point.x, point.height, point.y));
    })
    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    const material = new PointsMaterial({color: color, size: size});
    const points = new Points(pointsGeometry, material)
    this.scene.add(points);
  }

  drawTestSplinePoints() {
    const testPoints = [
      new LayerPoint(3374.91522140110, 1837.62935282490, 200),
      new LayerPoint(3407.00928489603, 1837.62935282490, 200),
      new LayerPoint(3449.80136955593, 1841.19535987989, 200),
      new LayerPoint(3487.24444363335, 1841.19535987989, 200),
      new LayerPoint(3535.38553887574, 1844.76136693488, 200),
      new LayerPoint(3567.47960237067, 1835.84634929740, 200),
      new LayerPoint(3587.09264117313, 1837.62935282490, 200),
      new LayerPoint(3613.83769408557, 1846.54437046238, 200),
      new LayerPoint(3633.45073288802, 1850.11037751737, 200),
      new LayerPoint(3658.41278227297,1866.15740926484, 200),
      new LayerPoint(3661.97878932796, 1882.20444101230, 200),
      new LayerPoint(3663.76179285546, 1907.16649039725, 200),
      new LayerPoint(3667.32779991045, 1933.91154330969, 200),
      new LayerPoint(3663.76179285546, 1960.65659622213, 200),
      new LayerPoint(3651.28076816298, 1980.26963502458, 200),
      new LayerPoint(3638.79974347051, 1996.31666677205, 200),
      new LayerPoint(3626.31871877804, 2007.01468793702, 200),
      new LayerPoint(3606.70567997558, 2021.27871615699, 200)

      							// 3594.22465528311	3574.61161648065	3563.91359531568	3554.99857767820	3538.95154593073	3517.55550360078	3492.59345421584	3472.98041541338	3439.10334839096	3408.79228842352	3376.69822492860	3337.47214732368	3316.07610499373	3300.02907324627	3292.89705913628	3267.93500975134	3244.75596389389	3234.05794272892	3223.35992156394	3210.87889687147	3201.96387923399	3194.83186512400	3191.26585806901	3185.91684748652	3171.65281926655	3166.30380868407	3157.38879104659	3168.08681221156	3189.48285454151	3212.66190039896	3239.40695331140	3264.36900269635	3287.54804855380	3307.16108735625	3374.91522140110
      							// 2024.84472321198	2040.89175495945	2060.50479376190	2074.76882198187	2087.24984667434	2099.73087136682	2110.42889253179	2117.56090664178	2119.34391016927	2121.12691369677	2122.90991722426	2122.90991722426	2119.34391016927	2144.30595955422	2151.43797366420	2153.22097719170	2149.65497013670	2142.52295602672	2121.12691369677	2101.51387489431	2096.16486431182	2081.90083609186	2071.20281492688	2046.24076554193	2024.84472321198	1999.88267382704	1969.57161385961	1942.82656094717	1928.56253272720	1914.29850450723	1900.03447628726	1892.90246217728	1875.07242690232	1855.45938809986	1837.62935282490
    ]
    const testLayer = new Layer();
    testLayer.points = testPoints;
    this.drawLayersPoints([testLayer], 0xff1215, 55);
  }

  drawTestPoint() {
    const point = new LayerPoint(2720, 3143, 4*200);
    const testLayer = new Layer();
    testLayer.points = [point];
    this.drawLayersPoints([testLayer], 0xff1215, 1);
  }


  drawTestPoints() {
    //0.8%
    const testPoints = [
      new LayerPoint(1139, 3063, 2400 ),
      new LayerPoint(1172,3065, 2200 ),
      //
      new LayerPoint(1874, 2409, 2200),
      new LayerPoint(1864, 2432, 2000),
      //
      new LayerPoint(2016, 3587, 2000),
      new LayerPoint(2039, 3568, 1800),
      //
      new LayerPoint(1748,3646, 1800),
      new LayerPoint(1751, 3661, 1600),
      //
      new LayerPoint(2287, 2644, 1600),
      new LayerPoint(2294, 2627, 1400),
      //
      new LayerPoint(1960, 2599, 1400),
      new LayerPoint(1963, 2594, 1200),
      //
      new LayerPoint(2099, 3512, 1200),
      new LayerPoint(2098, 3507, 1000),
      //
      new LayerPoint(1953, 2593, 1000),
      new LayerPoint(1934, 2589, 800),
      //
      new LayerPoint(1812, 2461, 800),
      new LayerPoint(1828, 2470, 600),
      //
      new LayerPoint(1256,3133 , 600),
      new LayerPoint(1230, 3101, 400),
      //
      new LayerPoint(1785, 3269, 400),
      new LayerPoint(1853, 3310, 200)
      //
    ]
    const testLayer = new Layer();
    testLayer.points = testPoints;
    this.drawLayersPoints([testLayer], 0xff1215, 55);
  }

  translatePoints(layerTranslations: Map<number, Array<number>>) {
    layerTranslations.forEach(([x, y, z], key) => {
      console.log("test", x, y, z, key);
      //todo: uzupełnić kiedy będziemy zwracać listę trójkątów dla każdej warstwy
    })
  }

}

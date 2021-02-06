import { Injectable } from '@angular/core';
import {BufferGeometry, Points, PointsMaterial, Vector3} from "three";

@Injectable({
  providedIn: 'root'
})
export class TriangleService {

  constructor() { }

  public createMockLayeredPoints() {
    const centerPoint = [2500, 2500];
    const maxRadius = 2000;
    const minRadius = 0;
    const fiStep = 2.5;
    const layersNumber = 16;
    const layerHeight = 50;
    let pointsCoords = []


    // points
    for(let layerId = 0; layerId < layersNumber; layerId++) {
      let radius = -2*(minRadius-maxRadius)*layerId*layerId/(layersNumber*layersNumber)+2*(minRadius-maxRadius)*layerId/layersNumber+minRadius;

      for (let fi = 0; fi <= 2 * Math.PI; fi += (fiStep-fiStep*0.87*Math.sin(layerId/(2*Math.PI)))+(Math.random()-0.5)/6) {
        let x = centerPoint[0] + radius * Math.cos(fi) + Math.random() * radius/30 ;
        let z = centerPoint[1] + radius * Math.sin(fi) + Math.random() * radius/30 ;
        pointsCoords.push(new Vector3(x, layerId*layerHeight, z));
      }
    }




    console.log("points count", pointsCoords.length)

    const pointsGeometry = new BufferGeometry().setFromPoints(pointsCoords);
    return new Points(pointsGeometry, new PointsMaterial({color: 0x0000ff, size: 20}));
  }


}

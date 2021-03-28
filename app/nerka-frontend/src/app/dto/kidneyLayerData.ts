import {Points} from "three";
import {ObjectType} from "./objectType";
import {RawLayer} from "./rawLayer";

export class KidneyLayerData {
  layerPoints: Points;
  layerName: string;
  objectType: ObjectType;
  layerHeight: number;
  rawLayer: RawLayer;
  isVisible: boolean;


  constructor(layerPoints: Points, layerName: string, objectType: ObjectType, layerHeight: number, rawLayer: RawLayer) {
    this.layerPoints = layerPoints;
    this.layerName = layerName;
    this.objectType = objectType;
    this.layerHeight = layerHeight;
    this.rawLayer = rawLayer;
    this.isVisible = true;
  }
}

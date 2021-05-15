import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {RawLayerFromBackend} from "../dto/rawLayerFromBackend";
import {LayerPoint} from "../dto/layerPoint";
import {Layer} from "../dto/layer";
import {Triangle} from "../dto/triangle";

@Injectable({
  providedIn: 'root'
})
export class BackendDataLoaderService {

  constructor(private http: HttpClient) { }

  public getTestLayer(): Observable<RawLayerFromBackend> {
    return this.http.get<RawLayerFromBackend>("/api/test");
  }

  public getTestBorder(): Observable<LayerPoint[]> {
    return this.http.get<LayerPoint[]>("/api/border");
  }

  public getTestLayers(): Observable<Layer[]> {
    return this.http.get<Layer[]>("/api/layers");
  }

  public getTestTriangles(): Observable<Triangle[]> {
    return this.http.get<Triangle[]>("/api/triangles");
  }

  public getTrianglesFromFiles(files): Observable<Triangle[]> {
    let formData = new FormData();
    for(let i=0; i<files.length; i++) {
      formData.append("files", files[i]);
    }
    return this.http.post<Triangle[]>("/api/trianglesFromFiles", formData);
  }

}

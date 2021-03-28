export enum ObjectType {
  Kidney, Cancer, Other
}

export namespace ObjectType {
  export function fromString(input: string): ObjectType {
    input = input.trim().toLowerCase();
    switch (input) {
      case "kidney": return ObjectType.Kidney;
      case "cancer": return ObjectType.Cancer;
      default: return ObjectType.Other;
    }
  }
}

//제네릭
export type ActionResult<T> = 
  | { result: 'success'; data: T }
  | { result: 'error'; message: string }; 
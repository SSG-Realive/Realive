export const SEP = '|';

export interface AddressParts {
  zonecode: string;
  address: string;
  detail: string;
}

/** "48060|도로명주소|상세" → { zonecode, address, detail } */
export function parseAddress(str = ''): AddressParts {
  const [zonecode = '', addr = '', detail = ''] = str.split(SEP);
  return { zonecode, address: addr, detail };
}

/** { zonecode, address, detail } → "48060|도로명주소|상세" */
export function joinAddress(p: AddressParts): string {
  return [p.zonecode, p.address, p.detail].join(SEP);
}

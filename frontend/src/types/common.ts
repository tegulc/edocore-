export interface PageDto<T> {
  donnees: T[];
  pagination: {
    page: number;
    taille: number;
    total: number;
    pagesTotales: number;
  };
}

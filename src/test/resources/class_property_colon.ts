// Class properties with type annotations
class OrderService {
  private readonly pixartOrderClient: PixartOrderClient;
  private config: Config;
  readonly name: string;
}

// Also test in a more generic context
class GenericService {
  private client: HttpClient;
  protected logger: Logger;
  public timeout: number;
}


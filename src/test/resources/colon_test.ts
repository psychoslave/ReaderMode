// Test all colon contexts in TypeScript

// 1. Class property with type annotation
class ServiceClass {
  private readonly client: object;
  readonly config: Record<string, any>;

  // 2. Constructor parameter with type annotation
  constructor(
    logger: Console,
    private readonly locale: string,
  ) {}
}

// 3. Object literal with properties
const options = {
  timeout: 5000,
  retryCount: 3,
};

// 4. Destructuring with rename
const { data: result, error } = { data: "test", error: null };

// 5. Type/interface annotations
interface ApiResponse {
  status: number;
  body: string;
}

type Configuration = {
  enabled: boolean;
  timeout: number;
};

// 6. Function parameter type
function process(input: string): void {}

// 7. Arrow function type
const handler: (x: number) => string = (x) => x.toString();




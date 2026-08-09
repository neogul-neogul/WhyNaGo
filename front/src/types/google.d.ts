/**
 * Google Identity Services(GIS) 전역 객체 타입.
 * SDK를 스크립트 태그로 로드하므로(npm 패키지 없음) 사용하는 부분만 선언한다.
 */

interface GoogleCredentialResponse {
  /** 구글이 서명한 id_token. 백엔드가 검증한다 */
  credential: string;
}

interface GoogleIdConfiguration {
  client_id: string;
  callback: (response: GoogleCredentialResponse) => void;
}

interface GoogleButtonOptions {
  type?: "standard" | "icon";
  theme?: "outline" | "filled_blue" | "filled_black";
  size?: "large" | "medium" | "small";
  text?: "signin_with" | "signup_with" | "continue_with" | "signin";
  shape?: "rectangular" | "pill" | "circle" | "square";
  logo_alignment?: "left" | "center";
  width?: number;
  locale?: string;
}

interface Window {
  google?: {
    accounts: {
      id: {
        initialize: (config: GoogleIdConfiguration) => void;
        renderButton: (parent: HTMLElement, options: GoogleButtonOptions) => void;
        disableAutoSelect: () => void;
      };
    };
  };
}

const NEXT_PUBLIC_BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

// 백엔드 OAuth2 리디렉션 URL
const GOOGLE_REDIRECT_URI = `${NEXT_PUBLIC_BACKEND_URL}/login/oauth2/code/google`;
const GITHUB_REDIRECT_URI = `${NEXT_PUBLIC_BACKEND_URL}/login/oauth2/code/github`;

// 프론트엔드 OAuth2 인증 완료 후 리디렉션될 URL
const OAUTH_REDIRECT_PATH = "/oauth/redirect";

export const apiConfig = {
  apiUrl: NEXT_PUBLIC_BACKEND_URL,
  oauth: {
    google: {
      authorizationUrl: `/oauth2/authorization/google?redirect_uri=${OAUTH_REDIRECT_PATH}`,
    },
    github: {
      authorizationUrl: `/oauth2/authorization/github?redirect_uri=${OAUTH_REDIRECT_PATH}`,
    },
  },
};

// 환경 변수가 제대로 설정되었는지 확인 (서버 사이드에서만 실행)
if (typeof window === "undefined" && !NEXT_PUBLIC_BACKEND_URL) {
  console.error("********************************************************************************");
  console.error("ERROR: NEXT_PUBLIC_BACKEND_URL 환경 변수가 설정되지 않았습니다.");
  console.error("frontend/.env.local 파일을 생성하고 아래와 같이 변수를 추가해주세요.");
  console.error("NEXT_PUBLIC_BACKEND_URL=http://localhost:8080");
  console.error("********************************************************************************");
}

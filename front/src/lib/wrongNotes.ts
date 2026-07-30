import { apiFetch } from "@/lib/api";
import type {
  WrongNoteBookmarkResponse,
  WrongNoteDetailResponse,
  WrongNoteSummaryResponse,
} from "@/types";

// 오답노트 도메인 API — 상태·반복 횟수·출처는 두지 않으므로 필터는 북마크 여부뿐이다.

/** 오답노트 목록 조회. bookmarked를 생략하면 전체를 반환한다 */
export function fetchWrongNotes(bookmarked?: boolean): Promise<WrongNoteSummaryResponse[]> {
  const query = bookmarked === undefined ? "" : `?bookmarked=${bookmarked}`;
  return apiFetch<WrongNoteSummaryResponse[]>(`/api/wrong-notes${query}`);
}

/** 오답노트 상세 조회 */
export function fetchWrongNoteDetail(wrongNoteId: number): Promise<WrongNoteDetailResponse> {
  return apiFetch<WrongNoteDetailResponse>(`/api/wrong-notes/${wrongNoteId}`);
}

/** 오답노트 북마크 상태 변경 */
export function updateWrongNoteBookmark(
  wrongNoteId: number,
  bookmarked: boolean,
): Promise<WrongNoteBookmarkResponse> {
  return apiFetch<WrongNoteBookmarkResponse>(`/api/wrong-notes/${wrongNoteId}/bookmark`, {
    method: "PATCH",
    body: { bookmarked },
  });
}

/** 오답노트 삭제 */
export function deleteWrongNote(wrongNoteId: number): Promise<void> {
  return apiFetch<void>(`/api/wrong-notes/${wrongNoteId}`, { method: "DELETE" });
}

/** ISO LocalDateTime("2026-06-25T10:00:00") → "2026.06.25" */
export function formatSolvedDate(solvedAt: string): string {
  return solvedAt.slice(0, 10).replaceAll("-", ".");
}

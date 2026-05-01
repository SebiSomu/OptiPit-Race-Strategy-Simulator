/**
 * Formats seconds into F1 standard time format (H:M:S.ms)
 * @param seconds - time in seconds
 * @param showMillis - whether to include milliseconds
 */
export function formatF1Time(seconds: number, showMillis: boolean = true): string {
  if (seconds === 0) return "0.000";
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  
  const secondsPart = showMillis 
    ? remainingSeconds.toFixed(3) 
    : Math.floor(remainingSeconds).toString();

  const paddedSeconds = parseFloat(secondsPart) < 10 && (hours > 0 || minutes > 0)
    ? `0${secondsPart}` 
    : secondsPart;

  if (hours > 0) {
    const paddedMinutes = minutes < 10 ? `0${minutes}` : minutes;
    return `${hours}:${paddedMinutes}:${paddedSeconds}`;
  }
  
  if (minutes > 0) {
    return `${minutes}:${paddedSeconds}`;
  }
  
  return secondsPart;
}

/**
 * Formats delta times (e.g. +1.234s)
 */
export function formatF1Delta(seconds: number): string {
  if (seconds === 0) return "OPTIMAL";
  return `+${seconds.toFixed(3)}s`;
}

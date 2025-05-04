// カレンダーで選択できる最大の日付は3か月後
let maxDate = new Date();
maxDate = maxDate.setMonth(maxDate.getMonth() + 3);

flatpickr("#reservationDateTime", {
	// 時間を有効にする
    enableTime: true,  
    // 24時間制にする     
    time_24hr: true, 
    // yyyy-MM-dd HH:mm の形式で設定       
    dateFormat: "Y-m-d H:i",
    // カレンダーの言語を日本語
    locale: 'ja', 
    // カレンダーで選択できる最小の日付を当日
    minDate: 'today',
    // カレンダーで選択できる最大の日付を3か月後
    maxDate: maxDate
});

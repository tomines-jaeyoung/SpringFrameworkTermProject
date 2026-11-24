/* 첫 화면 소개 영상: 화면에 보일 때 자동 재생(음소거), 재생/일시정지·소리 버튼 */
(function () {
  var NS = 'http://www.w3.org/2000/svg';
  var ICON = {
    pause: '<rect x="14" y="3" width="5" height="18" rx="1"></rect><rect x="5" y="3" width="5" height="18" rx="1"></rect>',
    play: '<path d="M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z"></path>',
    on: '<path d="M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z"></path><path d="M16 9a5 5 0 0 1 0 6"></path><path d="M19.364 18.364a9 9 0 0 0 0-12.728"></path>',
    off: '<path d="M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z"></path><line x1="22" x2="16" y1="9" y2="15"></line><line x1="16" x2="22" y1="9" y2="15"></line>'
  };
  function setIcon(btn, key) { var svg = btn.querySelector('svg'); if (svg) svg.innerHTML = ICON[key]; }
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  document.querySelectorAll('.oa-phone').forEach(function (phone) {
    var video = phone.querySelector('video');
    var buttons = phone.querySelectorAll('.oa-video-controls button');
    if (!video || buttons.length < 2) return;
    var playBtn = buttons[0], muteBtn = buttons[1], visible = false, requested = !reduce;
    video.muted = true;
    function sync() {
      if (visible && requested && document.visibilityState === 'visible') video.play().catch(function () {});
      else video.pause();
    }
    video.addEventListener('play', function () { setIcon(playBtn, 'pause'); playBtn.setAttribute('aria-label', '영상 일시정지'); });
    video.addEventListener('pause', function () { setIcon(playBtn, 'play'); playBtn.setAttribute('aria-label', '영상 재생'); });
    playBtn.addEventListener('click', function () {
      if (video.paused) { requested = true; video.play().catch(function () {}); } else { requested = false; video.pause(); }
    });
    muteBtn.addEventListener('click', function () {
      video.muted = !video.muted;
      setIcon(muteBtn, video.muted ? 'off' : 'on');
      muteBtn.setAttribute('aria-label', video.muted ? '영상 소리 켜기' : '영상 음소거');
    });
    if ('IntersectionObserver' in window) {
      new IntersectionObserver(function (e) { visible = e[0].isIntersecting; sync(); }, { threshold: .15 }).observe(video);
    } else { visible = true; sync(); }
    document.addEventListener('visibilitychange', sync);
  });
})();

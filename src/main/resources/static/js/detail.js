document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');

    if (!id) {
        document.getElementById('detail').innerHTML = '<div class="empty">参数错误</div>';
        return;
    }

    fetch('/api/competitions/' + id)
        .then(res => {
            if (!res.ok) throw new Error('Not found');
            return res.json();
        })
        .then(data => {
            // 直接跳转到原文链接
            if (data.url) {
                window.location.href = data.url;
            } else {
                document.getElementById('detail').innerHTML = '<div class="empty">原文链接不可用</div>';
            }
        })
        .catch(() => {
            document.getElementById('detail').innerHTML = '<div class="empty">公告不存在或加载失败</div>';
        });
});

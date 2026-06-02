package com.example.ui

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ES_LA("es_LA", "Español Latino", "🇲🇽"),
    EN_GB("en_GB", "British English", "🇬🇧"),
    PT_BR("pt_BR", "Português", "🇧🇷"),
    RU_RU("ru_RU", "Русский", "🇷🇺"),
    JA_JP("ja_JP", "日本語", "🇯🇵")
}

object TranslationManager {
    private val dictionary: Map<String, Map<AppLanguage, String>> = mapOf(
        "APP_TITLE" to mapOf(
            AppLanguage.ES_LA to "AP ORGANIZATION",
            AppLanguage.EN_GB to "AP ORGANIZATION",
            AppLanguage.PT_BR to "AP ORGANIZAÇÃO",
            AppLanguage.RU_RU to "AP ОРГАНИЗАЦИЯ",
            AppLanguage.JA_JP to "AP オーガニゼーション"
        ),
        "APP_SUBTITLE" to mapOf(
            AppLanguage.ES_LA to "REPRODUCTOR DE ALTA RESOLUCIÓN",
            AppLanguage.EN_GB to "HIGH-RESOLUTION PLAYER",
            AppLanguage.PT_BR to "REPRODUTOR DE ALTA RESOLUÇÃO",
            AppLanguage.RU_RU to "ПЛЕЕР ВЫСОКОГО РАЗРЕШЕНИЯ",
            AppLanguage.JA_JP to "高解像度プレーヤー"
        ),
        "TAB_MUSIC" to mapOf(
            AppLanguage.ES_LA to "Música",
            AppLanguage.EN_GB to "Music",
            AppLanguage.PT_BR to "Música",
            AppLanguage.RU_RU to "Музыка",
            AppLanguage.JA_JP to "音楽"
        ),
        "TAB_VIDEO" to mapOf(
            AppLanguage.ES_LA to "Video",
            AppLanguage.EN_GB to "Video",
            AppLanguage.PT_BR to "Vídeo",
            AppLanguage.RU_RU to "Видео",
            AppLanguage.JA_JP to "ビデオ"
        ),
        "TAB_ALBUMS" to mapOf(
            AppLanguage.ES_LA to "Álbumes",
            AppLanguage.EN_GB to "Albums",
            AppLanguage.PT_BR to "Álbuns",
            AppLanguage.RU_RU to "Альбомы",
            AppLanguage.JA_JP to "アルバム"
        ),
        "TAB_RECENT" to mapOf(
            AppLanguage.ES_LA to "Reciente",
            AppLanguage.EN_GB to "Recent",
            AppLanguage.PT_BR to "Recente",
            AppLanguage.RU_RU to "Недавние",
            AppLanguage.JA_JP to "最近"
        ),
        "TAB_STORAGE" to mapOf(
            AppLanguage.ES_LA to "Almacenamiento",
            AppLanguage.EN_GB to "Storage",
            AppLanguage.PT_BR to "Armazenamento",
            AppLanguage.RU_RU to "Хранилище",
            AppLanguage.JA_JP to "ストレージ"
        ),
        "SCAN_BUTTON" to mapOf(
            AppLanguage.ES_LA to "ESCANEAR",
            AppLanguage.EN_GB to "SCAN",
            AppLanguage.PT_BR to "ESCANEAR",
            AppLanguage.RU_RU to "СКАНИРОВАТЬ",
            AppLanguage.JA_JP to "スキャン"
        ),
        "SCANNING_TOAST" to mapOf(
            AppLanguage.ES_LA to "Buscando pistas locales de sonido y video...",
            AppLanguage.EN_GB to "Scanning local audio and video tracks...",
            AppLanguage.PT_BR to "Buscando faixas de som e vídeo locais...",
            AppLanguage.RU_RU to "Поиск локальных аудио и видео треков...",
            AppLanguage.JA_JP to "ローカルのオーディオとビデオトラックをスキャン中..."
        ),
        "SMART_PLAYLIST" to mapOf(
            AppLanguage.ES_LA to "LISTA INTELIGENTE (Frecuencia activa)",
            AppLanguage.EN_GB to "SMART PLAYLIST (Active flow)",
            AppLanguage.PT_BR to "PLAYLIST INTELIGENTE (Frequência ativa)",
            AppLanguage.RU_RU to "УМНЫЙ ПЛЕЙЛИСТ (Активная частота)",
            AppLanguage.JA_JP to "スマートプレイリスト（アクティブな再生頻度）"
        ),
        "SMART_EMPTY" to mapOf(
            AppLanguage.ES_LA to "Escucha más canciones para poblar frecuencia...",
            AppLanguage.EN_GB to "Listen to more tracks to populate frequency...",
            AppLanguage.PT_BR to "Ouça mais músicas para preencher a frequência...",
            AppLanguage.RU_RU to "Слушайте больше треков, чтобы заполнить список...",
            AppLanguage.JA_JP to "再生頻度を高めるためにより多くの曲を聴いてください..."
        ),
        "MUSIC_HEADER" to mapOf(
            AppLanguage.ES_LA to "NUESTRAS PISTAS DISPONIBLES",
            AppLanguage.EN_GB to "AVAILABLE AUDIO TRACKS",
            AppLanguage.PT_BR to "NOSSAS FAIXAS DISPONÍVEIS",
            AppLanguage.RU_RU to "ДОСТУПНЫЕ АУДИОТРЕКИ",
            AppLanguage.JA_JP to "利用可能なトラック"
        ),
        "MUSIC_EMPTY" to mapOf(
            AppLanguage.ES_LA to "Sin pistas en base de datos. Haz clic en Escaneo o Demo.",
            AppLanguage.EN_GB to "No tracks in database. Click Scan or Demo.",
            AppLanguage.PT_BR to "Sem faixas no banco de dados. Clique em Escanear ou Demo.",
            AppLanguage.RU_RU to "Нет треков в базе данных. Нажмите Сканировать или Демо.",
            AppLanguage.JA_JP to "データベースにトラックがありません。スキャンまたはデモをクリックしてください。"
        ),
        "FOLDER_LOCATOR" to mapOf(
            AppLanguage.ES_LA to "LOCALIZADOR DE CARPETAS DE VIDEOS",
            AppLanguage.EN_GB to "VIDEO FOLDER LOCATOR",
            AppLanguage.PT_BR to "LOCALIZADOR DE PASTAS DE VÍDEO",
            AppLanguage.RU_RU to "ЛОКАТОР ПАПОК С ВИДЕО",
            AppLanguage.JA_JP to "ビデオフォルダーロケーター"
        ),
        "ALL_FOLDERS" to mapOf(
            AppLanguage.ES_LA to "TODOS",
            AppLanguage.EN_GB to "ALL",
            AppLanguage.PT_BR to "TODOS",
            AppLanguage.RU_RU to "ВСЕ",
            AppLanguage.JA_JP to "すべて"
        ),
        "NO_VIDEOS" to mapOf(
            AppLanguage.ES_LA to "Ningún archivo de video cargado en esta sección.",
            AppLanguage.EN_GB to "No video files found in this section.",
            AppLanguage.PT_BR to "Nenhum arquivo de vídeo carregado nesta seção.",
            AppLanguage.RU_RU to "В этом разделе не найдено видеофайлов.",
            AppLanguage.JA_JP to "このセクションにビデオファイルが見つかりません。"
        ),
        "ALBUMS_HEADER" to mapOf(
            AppLanguage.ES_LA to "ÁLBUMES DISPONIBLES EN SISTEMA",
            AppLanguage.EN_GB to "AVAILABLE ALBUMS IN SYSTEM",
            AppLanguage.PT_BR to "ÁLBUNS DISPONÍVEIS NO SISTEMA",
            AppLanguage.RU_RU to "ДОСТУПНЫЕ АЛЬБОМЫ В СИСТЕМЕ",
            AppLanguage.JA_JP to "システム内で利用可能なアルバム"
        ),
        "LOADING_ALBUMS" to mapOf(
            AppLanguage.ES_LA to "Cargando álbumes...",
            AppLanguage.EN_GB to "Loading albums...",
            AppLanguage.PT_BR to "Carregando álbuns...",
            AppLanguage.RU_RU to "Загрузка альбомов...",
            AppLanguage.JA_JP to "アルバムを読み込み中..."
        ),
        "RECENT_HEADER" to mapOf(
            AppLanguage.ES_LA to "NUEVOS ARCHIVOS DE REPRODUCCIÓN DETECTADOS",
            AppLanguage.EN_GB to "NEW PLAYBACK FILES DETECTED",
            AppLanguage.PT_BR to "NOVOS ARQUIVOS DE REPRODUÇÃO DETECTADOS",
            AppLanguage.RU_RU to "ОБНАРУЖЕНЫ НОВЫЕ ФАЙЛЫ ДЛЯ ВОСПРОИЗВЕДЕНИЯ",
            AppLanguage.JA_JP to "新しく検出された再生ファイル"
        ),
        "RECENT_EMPTY" to mapOf(
            AppLanguage.ES_LA to "No se detectaron añadidos recientes...",
            AppLanguage.EN_GB to "No recent additions detected...",
            AppLanguage.PT_BR to "Nenhuma adição recente detectada...",
            AppLanguage.RU_RU to "Недавних добавлений не обнаружено...",
            AppLanguage.JA_JP to "最近の追加は検出されませんでした..."
        ),
        "STORAGE_HEADER" to mapOf(
            AppLanguage.ES_LA to "GESTIÓN DE MEMORIAS Y TARJETA SD",
            AppLanguage.EN_GB to "STORAGE & SD CARD MANAGEMENT",
            AppLanguage.PT_BR to "GERENCIAMENTO DE MEMÓRIA E CARTÃO SD",
            AppLanguage.RU_RU to "УПРАВЛЕНИЕ ПАМЯТЬЮ И SD-КАРТОЙ",
            AppLanguage.JA_JP to "メモリおよびSDカード管理"
        ),
        "STORAGE_DESC" to mapOf(
            AppLanguage.ES_LA to "Control de acceso directo y catalogado de pistas físicas en tarjetas de memoria microSD y almacenamiento local.",
            AppLanguage.EN_GB to "Direct access control and cataloging of physical tracks on microSD memory cards and local storage.",
            AppLanguage.PT_BR to "Controle de acesso direto e catalogação de faixas físicas em cartões de memória microSD e armazenamento local.",
            AppLanguage.RU_RU to "Прямой контроль доступа и каталогизация физических треков на картах памяти microSD и локальном хранилище.",
            AppLanguage.JA_JP to "microSDメモリーカードおよびローカルストレージ上の物理トラックの直接アクセス制御とカタログ化。"
        ),
        "MEDIA_PERMISSIONS" to mapOf(
            AppLanguage.ES_LA to "PERMISOS DE MEDIOS",
            AppLanguage.EN_GB to "MEDIA PERMISSIONS",
            AppLanguage.PT_BR to "PERMISSÕES DE MÍDIA",
            AppLanguage.RU_RU to "РАЗРЕШЕНИЯ НА МЕДИА",
            AppLanguage.JA_JP to "メディア権限"
        ),
        "PERMISSION_GRANTED" to mapOf(
            AppLanguage.ES_LA to "CONCEDIDO",
            AppLanguage.EN_GB to "GRANTED",
            AppLanguage.PT_BR to "CONCEDIDO",
            AppLanguage.RU_RU to "ПРЕДОСТАВЛЕНО",
            AppLanguage.JA_JP to "許可済み"
        ),
        "PERMISSION_REQUIRED" to mapOf(
            AppLanguage.ES_LA to "REQUERIDO",
            AppLanguage.EN_GB to "REQUIRED",
            AppLanguage.PT_BR to "REQUERIDO",
            AppLanguage.RU_RU to "ТРЕБУЕТСЯ",
            AppLanguage.JA_JP to "必須"
        ),
        "PERMISSION_GRANTED_DESC" to mapOf(
            AppLanguage.ES_LA to "Acceso desbloqueado al sistema de archivos local y tarjetas SD instaladas de forma óptima. El reproductor AP puede leer directamente tus medios en alta definición.",
            AppLanguage.EN_GB to "Unlocked access to local files and installed SD cards. AP Player can read your high-definition media directly.",
            AppLanguage.PT_BR to "Acesso desbloqueado a arquivos locais e cartões SD instalados. O AP Player pode ler diretamente sua mídia de alta definição.",
            AppLanguage.RU_RU to "Разблокирован доступ к локальным файлам и установленным SD-картам. AP-плеер может воспроизводить ваши медиафайлы в высоком разрешении напрямую.",
            AppLanguage.JA_JP to "ローカルファイルおよびインストールされたSDカードへのアクセスを解除しました。APプレーヤーは高解像度メディアを直接読み取ることができます。"
        ),
        "PERMISSION_REQUIRED_DESC" to mapOf(
            AppLanguage.ES_LA to "Para buscar pistas de música o documentales de video almacenados físicamente en la memoria interna o externa (micro SD), es necesario autorizar los accesos a los medios.",
            AppLanguage.EN_GB to "To scan for music or video documents stored on internal or external memory (micro SD), media access must be authorized.",
            AppLanguage.PT_BR to "Para escanear músicas ou vídeos armazenados na memória interna ou externa (micro SD), o acesso à mídia deve ser autorizado.",
            AppLanguage.RU_RU to "Для сканирования музыки или видео, хранящихся на внутренней или внешней памяти (micro SD), необходимо разрешить доступ к медиафайлам.",
            AppLanguage.JA_JP to "内部または外部メモリ（micro SD）に保存されている音楽やビデオをスキャンするには、メディアアクセスを許可する必要があります。"
        ),
        "GRANT_PERMISSION_BTN" to mapOf(
            AppLanguage.ES_LA to "OTORGAR ACCESO A MEDIOS",
            AppLanguage.EN_GB to "GRANT MEDIA ACCESS",
            AppLanguage.PT_BR to "CONCEDER ACESSO À MÍDIA",
            AppLanguage.RU_RU to "ПРЕДОСТАВИТЬ ДОСТУП К МЕДИА",
            AppLanguage.JA_JP to "メディアアクセスを許可する"
        ),
        "VOLUMES_TITLE" to mapOf(
            AppLanguage.ES_LA to "VOLÚMENES Y DISPOSITIVOS DIRECTOS",
            AppLanguage.EN_GB to "VOLUMES & DIRECT DEVICES",
            AppLanguage.PT_BR to "VOLUMES E DISPOSITIVOS DIRETOS",
            AppLanguage.RU_RU to "ТОМА И ПРЯМЫЕ УСТРОЙСТВА",
            AppLanguage.JA_JP to "ボリュームと直接デバイス"
        ),
        "LOADING_VOLUMES" to mapOf(
            AppLanguage.ES_LA to "Cargando discos de almacenamiento...",
            AppLanguage.EN_GB to "Loading storage drives...",
            AppLanguage.PT_BR to "Carregando unidades de armazenamento...",
            AppLanguage.RU_RU to "Загрузка накопителей...",
            AppLanguage.JA_JP to "ストレージドライブを読み込み中..."
        ),
        "SCAN_UNIT_BTN" to mapOf(
            AppLanguage.ES_LA to "ESCANEAR ESTA UNIDAD",
            AppLanguage.EN_GB to "SCAN THIS DRIVE",
            AppLanguage.PT_BR to "ESCANEAR ESTA UNIDADE",
            AppLanguage.RU_RU to "СКАНИРОВАТЬ ЭТОТ ДИСК",
            AppLanguage.JA_JP to "このドライブをスキャン"
        ),
        "SCANNING_UNIT_TOAST" to mapOf(
            AppLanguage.ES_LA to "Escaneo profundo en curso:",
            AppLanguage.EN_GB to "Deep scan in progress:",
            AppLanguage.PT_BR to "Escaneamento profundo em andamento:",
            AppLanguage.RU_RU to "Выполняется глубокое сканирование:",
            AppLanguage.JA_JP to "ディープスキャン実行中:"
        ),
        "INDEX_TITLE" to mapOf(
            AppLanguage.ES_LA to "INDICE DE ARCHIVOS GENERAL",
            AppLanguage.EN_GB to "GENERAL FILE INDEX",
            AppLanguage.PT_BR to "ÍNDICE GERAL DE ARQUIVOS",
            AppLanguage.RU_RU to "ОБЩИЙ ИНДЕКС ФАЙЛОВ",
            AppLanguage.JA_JP to "総合ファイルインデックス"
        ),
        "SONGS_LABEL" to mapOf(
            AppLanguage.ES_LA to "Canciones",
            AppLanguage.EN_GB to "Songs",
            AppLanguage.PT_BR to "Músicas",
            AppLanguage.RU_RU to "Песни",
            AppLanguage.JA_JP to "曲"
        ),
        "VIDEOS_LABEL" to mapOf(
            AppLanguage.ES_LA to "Videos",
            AppLanguage.EN_GB to "Videos",
            AppLanguage.PT_BR to "Vídeos",
            AppLanguage.RU_RU to "Видео",
            AppLanguage.JA_JP to "ビデオ"
        ),
        "TOTAL_INDEX_LABEL" to mapOf(
            AppLanguage.ES_LA to "Índice Total",
            AppLanguage.EN_GB to "Total Index",
            AppLanguage.PT_BR to "Índice Total",
            AppLanguage.RU_RU to "Общий индекс",
            AppLanguage.JA_JP to "総合インデックス"
        ),
        "SORT_OPTION_TITLE" to mapOf(
            AppLanguage.ES_LA to "OPCIONES DE ORDENAMIENTO",
            AppLanguage.EN_GB to "SORTING OPTIONS",
            AppLanguage.PT_BR to "OPÇÕES DE ORDENAÇÃO",
            AppLanguage.RU_RU to "ВАРИАНТЫ СОРТИРОВКИ",
            AppLanguage.JA_JP to "並べ替えオプション"
        ),
        "SORT_BY_TITLE" to mapOf(
            AppLanguage.ES_LA to "Título",
            AppLanguage.EN_GB to "Title",
            AppLanguage.PT_BR to "Título",
            AppLanguage.RU_RU to "Название",
            AppLanguage.JA_JP to "タイトル"
        ),
        "SORT_BY_ALBUM" to mapOf(
            AppLanguage.ES_LA to "Álbum",
            AppLanguage.EN_GB to "Album",
            AppLanguage.PT_BR to "Álbum",
            AppLanguage.RU_RU to "Альбом",
            AppLanguage.JA_JP to "アルバム"
        ),
        "SORT_BY_ARTIST" to mapOf(
            AppLanguage.ES_LA to "Artista",
            AppLanguage.EN_GB to "Artist",
            AppLanguage.PT_BR to "Artista",
            AppLanguage.RU_RU to "Исполнитель",
            AppLanguage.JA_JP to "アーティスト"
        ),
        "SORT_BY_DATE" to mapOf(
            AppLanguage.ES_LA to "Fecha",
            AppLanguage.EN_GB to "Date",
            AppLanguage.PT_BR to "Data",
            AppLanguage.RU_RU to "Дата",
            AppLanguage.JA_JP to "日付"
        ),
        "SORT_BY_DURATION" to mapOf(
            AppLanguage.ES_LA to "Duración",
            AppLanguage.EN_GB to "Duration",
            AppLanguage.PT_BR to "Duração",
            AppLanguage.RU_RU to "Длительность",
            AppLanguage.JA_JP to "時間"
        ),
        "SORT_ORDER_ASC" to mapOf(
            AppLanguage.ES_LA to "Ascendente",
            AppLanguage.EN_GB to "Ascending",
            AppLanguage.PT_BR to "Ascendente",
            AppLanguage.RU_RU to "По возрастанию",
            AppLanguage.JA_JP to "昇順"
        ),
        "SORT_ORDER_DESC" to mapOf(
            AppLanguage.ES_LA to "Descendente",
            AppLanguage.EN_GB to "Descending",
            AppLanguage.PT_BR to "Decrescente",
            AppLanguage.RU_RU to "По убыванию",
            AppLanguage.JA_JP to "降順"
        ),
        "CHOOSE_LANGUAGE" to mapOf(
            AppLanguage.ES_LA to "IDIOMA DE LA APP",
            AppLanguage.EN_GB to "APP LANGUAGE",
            AppLanguage.PT_BR to "IDIOMA DO APLICATIVO",
            AppLanguage.RU_RU to "ЯЗЫК ПРИЛОЖЕНИЯ",
            AppLanguage.JA_JP to "アプリの言語"
        ),
        "EQUALIZER_HEADER" to mapOf(
            AppLanguage.ES_LA to "ECUALIZADOR GRÁFICO",
            AppLanguage.EN_GB to "GRAPHIC EQUALIZER",
            AppLanguage.PT_BR to "EQUALIZER GRÁFICO",
            AppLanguage.RU_RU to "ГРАФИЧЕСКИЙ ЭКВАЛАЙЗЕР",
            AppLanguage.JA_JP to "グラフィックイコライザー"
        ),
        "EQUALIZER_DESC" to mapOf(
            AppLanguage.ES_LA to "Control de precisión acústica en 5 bandas de sonido de alta fidelidad.",
            AppLanguage.EN_GB to "Acoustic precision control across 5 high-fidelity sound bands.",
            AppLanguage.PT_BR to "Controle de precisão acústica em 5 bandas de som de alta fidelidade.",
            AppLanguage.RU_RU to "Управление акустической точностью в 5 высокоточных диапазонах.",
            AppLanguage.JA_JP to "5つの高音質サウンドバンドにわたる音響精密制御。"
        ),
        "EQUALIZER_PRESETS" to mapOf(
            AppLanguage.ES_LA to "PREAJUSTES DISPONIBLES",
            AppLanguage.EN_GB to "AVAILABLE PRESETS",
            AppLanguage.PT_BR to "PREDEFINIÇÕES DISPONÍVEIS",
            AppLanguage.RU_RU to "ДОСТУПНЫЕ ПРЕСЕТЫ",
            AppLanguage.JA_JP to "利用可能なプリセット"
        ),
        "EQUALIZER_CUSTOM" to mapOf(
            AppLanguage.ES_LA to "Añadir ajuste personalizado",
            AppLanguage.EN_GB to "Add custom preset",
            AppLanguage.PT_BR to "Adicionar predefinição personalizada",
            AppLanguage.RU_RU to "Добавить пользовательский пресет",
            AppLanguage.JA_JP to "カスタムプリセットを追加"
        ),
        "PLAYER_NOW_PLAYING" to mapOf(
            AppLanguage.ES_LA to "REPRODUCIENDO AHORA",
            AppLanguage.EN_GB to "NOW PLAYING",
            AppLanguage.PT_BR to "REPRODUZINDO AGORA",
            AppLanguage.RU_RU to "СЕЙЧАС ИГРАЕТ",
            AppLanguage.JA_JP to "現在再生中"
        ),
        "PLAYER_PREVIOUS" to mapOf(
            AppLanguage.ES_LA to "Anterior",
            AppLanguage.EN_GB to "Previous",
            AppLanguage.PT_BR to "Anterior",
            AppLanguage.RU_RU to "Предыдущий",
            AppLanguage.JA_JP to "前へ"
        ),
        "PLAYER_NEXT" to mapOf(
            AppLanguage.ES_LA to "Siguiente",
            AppLanguage.EN_GB to "Next",
            AppLanguage.PT_BR to "Próximo",
            AppLanguage.RU_RU to "Следующий",
            AppLanguage.JA_JP to "次へ"
        ),
        "HIRES_DESC" to mapOf(
            AppLanguage.ES_LA to "Sonido estereofónico premium optimizado para audífonos y parlantes.",
            AppLanguage.EN_GB to "Premium stereophonic sound optimized for headphones and speakers.",
            AppLanguage.PT_BR to "Som estereofônico premium otimizado para fones e alto-falantes.",
            AppLanguage.RU_RU to "Премиальный стереозвук, оптимизированный для наушников и колонок.",
            AppLanguage.JA_JP to "ヘッドフォンやスピーカーに最適化されたプレミアムステレオサウンド。"
        ),
        "PRESETS_TITLE" to mapOf(
            AppLanguage.ES_LA to "Preajustes disponibles:",
            AppLanguage.EN_GB to "Available presets:",
            AppLanguage.PT_BR to "Predefinições disponíveis:",
            AppLanguage.RU_RU to "Доступные пресеты:",
            AppLanguage.JA_JP to "利用可能なプリセット:"
        ),
        "CUSTOM_PRESET_LABEL" to mapOf(
            AppLanguage.ES_LA to "Nombre del Ajuste",
            AppLanguage.EN_GB to "Preset Name",
            AppLanguage.PT_BR to "Nome da Predefinição",
            AppLanguage.RU_RU to "Имя пресета",
            AppLanguage.JA_JP to "プリセット名"
        ),
        "TAP_TO_PLAY" to mapOf(
            AppLanguage.ES_LA to "Toca para reproducir",
            AppLanguage.EN_GB to "Tap to play",
            AppLanguage.PT_BR to "Toque para reproduzir",
            AppLanguage.RU_RU to "Нажмите для воспроизведения",
            AppLanguage.JA_JP to "タップして再生"
        ),
        "DEMO_TITLE" to mapOf(
            AppLanguage.ES_LA to "Buscando pistas locales...",
            AppLanguage.EN_GB to "Searching local tracks...",
            AppLanguage.PT_BR to "Procurando faixas locais...",
            AppLanguage.RU_RU to "Поиск локальных треков...",
            AppLanguage.JA_JP to "ローカルのトラックを検索中..."
        ),
        "AUDIO_LOCAL_LABEL" to mapOf(
            AppLanguage.ES_LA to "Audio Local",
            AppLanguage.EN_GB to "Local Audio",
            AppLanguage.PT_BR to "Áudio Local",
            AppLanguage.RU_RU to "Локальное аудио",
            AppLanguage.JA_JP to "ローカルオーディオ"
        ),
        "VIDEO_LOCAL_LABEL" to mapOf(
            AppLanguage.ES_LA to "Video Local",
            AppLanguage.EN_GB to "Local Video",
            AppLanguage.PT_BR to "Vídeo Local",
            AppLanguage.RU_RU to "Локальное видео",
            AppLanguage.JA_JP to "ローカルビデオ"
        ),
        "SAVE_AND_APPLY" to mapOf(
            AppLanguage.ES_LA to "GUARDAR Y APLICAR",
            AppLanguage.EN_GB to "SAVE & APPLY",
            AppLanguage.PT_BR to "SALVAR E APLICAR",
            AppLanguage.RU_RU to "СОХРАНИТЬ И ПРИМЕНИТЬ",
            AppLanguage.JA_JP to "保存して適用"
        ),
        "SYSTEM_APP_TITLE" to mapOf(
            AppLanguage.ES_LA to "REPRODUCTOR DE LA AP ORGANIZATION",
            AppLanguage.EN_GB to "AP ORGANIZATION PLAYER",
            AppLanguage.PT_BR to "REPRODUTOR AP ORGANIZATION",
            AppLanguage.RU_RU to "ПЛЕЕР AP ORGANIZATION",
            AppLanguage.JA_JP to "AP ORGANIZATION プレーヤー"
        )
    )

    fun translate(key: String, language: AppLanguage): String {
        return dictionary[key]?.get(language) ?: key
    }
}

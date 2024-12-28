## AnimeSceneLoader
这是一个还在实验和完善的Android App demo项目，旨在使用简单的图片素材和预定义编写的JSON格式的场景脚本来实现一个简单的动画或者PV场景播放器。</br>
该项目未依赖任何Android Native动画库和任何游戏引擎，而是使用了自定义的JSON结构来定义动画时间线，
以及通过该JSON格式实现了解析场景脚本和Android的SurfaceView来实现和展示动画和场景效果的动画引擎。</br>
目前引擎仅支持一些简单的动画效果以及部分非线性物理动画效果，但是还是希望能够通过这个项目来实现一个简单的动画播放器。</br>
代码写的很屎，但是还是希望能够有人能够看到这个项目，然后给我一些意见和建议来改善它。如果你也对这个项目感兴趣，欢迎fork和star。

### 资源包结构
```
ScenePackage/
│   ├── bgm/
│   │       └── bgm.mp3        用于存放背景音乐
│   ├── scene_background/      用于存放场景背景图片或者视频
│   ├── character/             用于存放场景中的角色图片
│   ├── effects_json/          可选，用于存放场景中的效果JSON
│   └── effects_sound/         可选，用于存放场景中的音效
└── scene.json                 整个场景的定义文件，定义资源包的基本信息和场景信息以及元素和时间轴
```

### 场景编排JSON格式
```json
{
  "name": "AnimeSceneTestScene",
  // 场景名称,用于在后续开发中实现场景信息的展示
  "version": "1.0.0",
  // 版本
  "description": "This is a test scene for AnimeSceneLoader",
  // 场景描述
  "author": "AnimeSceneLoader",
  // 作者
  "resources": {
    // 资源目录定义
    "bgm": "bgm/bgm.mp3",
    // 背景音乐，单个资源包只能定义一个背景音乐
    "scene_background_dir": "scene_background",
    // 场景背景目录
    "character_dir": "character",
    // 角色目录
    "effects_json_dir": "effects_json",
    // 效果JSON目录
    "effects_sound_dir": "effects_sound"
    // 效果音效目录
  },
  "scene_config": {
    "fps": 30,
    // 场景播放帧率上限，具体取决于设备性能
    "bgm_volume": 1.0,
    // 背景音乐音量
    "scene_duration_sync_with_bgm": true,
    // 场景持续时间是否与背景音乐相同
    "scene_duration": 0
    // 场景持续时间,如果上述定义为false,则定义该场景持续时间,bgm播放完毕后，场景根据该时间播放直到结束，若上述配置为true则忽略该项值
  },
  "scenes": [
    // 场景定义，单个资源包可以定义多个场景
    {
      "name": "MainScene",
      // 场景名称
      "description": "主场景",
      // 场景描述
      "background": [
        // 背景定义
        {
          "type": "image",
          // 背景类型
          "name": "background1",
          // 背景名称
          "path": "scene_background/bg1.png",
          // 背景图片路径
          "effect": "effects_json/bg_fade.json",
          // 背景效果JSON路径,目前还在开发中
          "duration": {
            "from": 0,
            "to": 5000
          }
          // 背景持续时间
        },
        {
          "type": "video",
          // 背景类型
          "name": "background2",
          // 背景名称
          "path": "scene_background/bg2.mp4",
          // 背景视频路径
          "effect": "effects_json/bg_transition.json",
          // 背景效果JSON路径
          "duration": {
            "from": 5000,
            "to": 10000
          }
          // 背景持续时间
        },
        {
          "type": "image",
          "name": "background1",
          "path": "scene_background/bg1.png",
          "effect": "effects_json/bg_fade.json",
          "duration": {
            "from": 0,
            "to": 5000
          }
        },
        {
          "type": "video",
          "name": "background2",
          "path": "scene_background/bg2.mp4",
          "effect": "effects_json/bg_transition.json",
          "duration": {
            "from": 5000,
            "to": 10000
          }
        }
      ],
      "elements": [
        // 元素定义，通常以角色为对象定义，定义单个场景内哪个元素应该在什么时间点出现，以及出现的效果
        {
          "type": "sprite", // 元素类型
          "name": "character1", // 元素名称
          "path": "character/alice.png", // 元素图片路径
          "z_index": 1, // 元素层级
          "scenes": [ // 该元素的时间轴，同一元素无法通过定义多个同一时间来达到单屏显示多个对象，若需要实现同屏多个相同对象同时显示则需要定义同资源但不同name的元素
            {
              "type": "fade_in", // 元素出现效果
              "duration": { // 元素出现效果持续时间
                "from": 0, // 开始时间
                "to": 1000 // 结束时间
              },
              "easing": "ease_in_out", // 元素出现效果缓动函数
              "from_alpha": 0.0, // 元素出现效果开始透明度
              "to_alpha": 1.0 // 元素出现效果结束透明度
            },
            {
              "type": "move", // 元素移动效果
              "duration": { // 元素移动效果持续时间
                "from": 1000, // 开始时间
                "to": 3000 // 结束时间
              },
              "easing": "ease_out", // 元素移动效果缓动函数
              "from": {
                "x": -100, // 元素移动效果开始位置x
                "y": 200 // 元素移动效果开始位置y
              },
              "to": { 
                "x": 300, // 元素移动效果结束位置x
                "y": 200 // 元素移动效果结束位置y
              }
            },
            {
              "type": "throw", // 元素抛物线效果（非线性动效）
              "duration": {
                "from": 3000, // 开始时间
                "to": 4000 // 结束时间
              },
              "from": {
                "x": 400, // 元素抛物线效果开始位置x
                "y": "screenBottom" // 元素抛物线效果开始位置y
              },
              "to": {
                "x": 500, // 元素抛物线效果结束位置x
                "y": "screenBottom" // 元素抛物线效果结束位置y
              }
            },
            {
              "type": "throw", 
              "duration": {
                "from": 6000,
                "to": 7000
              },
              "from": {
                "x": 1600,
                "y": "screenBottom"
              },
              "to": {
                "x": 1700,
                "y": "screenBottom"
              }
            }
          ]
        },
        {
          "type": "sprite", // 为达到同屏显示多个相同元素的动画效果，这里定义了一个相同资源但不同name的元素
          "name": "character2",
          "path": "character/alice.png",
          "z_index": 1,
          "scenes": [
            {
              "type": "throw",
              "duration": {
                "from": 3000,
                "to": 4000
              },
              "from": {
                "x": 200,
                "y": "screenBottom"
              },
              "to": {
                "x": 300,
                "y": "screenBottom"
              }
            },
            {
              "type": "throw",
              "duration": {
                "from": 6000,
                "to": 7000
              },
              "from": {
                "x": 1300,
                "y": "screenBottom"
              },
              "to": {
                "x": 1400,
                "y": "screenBottom"
              }
            }
          ]
        }
      ]
    }
  ]
}
```
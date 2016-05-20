
appcan-android
==============

appcan-android引擎

参考 http://newdocx.appcan.cn/

QQ交流群：173758265



### 生成引擎包步骤：

1. 新建Android Studio工程`Engine_AppCan`

2. clone 引擎代码到工程`Engine_AppCan`

   ```sh
   git clone https://github.com/AppCanOpenSource/appcan-android.git
   ```

3. 编辑工程根目录下的`settings.gradle`,在下面添加一行：
   ```groovy
   include ':appcan-android:Engine'
   ```

4. 按需修改引擎代码

5. 编辑`appcan-android/en_baseEngineProject/androidEngine.xml`里面的`description`信息，其他的不要改动

6. 在Android Studio自带的`Terminal`输入

   ```shell
   gradle buildEngine
   ```

7. 编译完成后，生成的引擎包在`appcan-android/Engine/build/outputs/engine`目录下



### 目录结构

```
├── Engine 
│   ├── Engine.iml
│   ├── build
│   ├── build.gradle
│   ├── libs  公用lib
│   ├── proguard.pro  混淆文件
│   ├── project.properties
│   └── src
├── LICENSE
├── README.md
├── appcan-android.iml
├── en_baseEngineProject
│   ├── WebkitCorePalm 
│   └── androidEngine.xml
└── gradle-plugin
    └── appcan-build-engine.jar
```

**Engine**：引擎工程，该工程的最终作用就是生成引擎的jar

当有除了`java`代码之外的改动，如添加图片资源，添加或修改布局文件，都需要同步改动到WebkitCorePalm（重要）

**en_baseEngineProject/WebkitCorePalm**：最终打包使用的工程，除了上面说的需要从`Engine`工程同步的内容，其他信息不要改动，

**Engine/build**:目录结构如下

```
├── appcan
│   ├── crosswalk
│   └── system
├── generated
├── intermediates
├── outputs
│   ├── engine
│   ├── jar
│   └── logs
└── tmp
```

**appcan**:生成引擎时产生的临时文件

**outputs/engine**:引擎输出目录

**outputs/jar**:`Engine`moudle编译出来的jar，以`-un-proguard.jar`结尾的为未混淆的jar，其他为混淆过的jar



### 关于混淆

- 混淆文件为：`appcan-android/Engine/proguard.pro`
- 混淆只用于混淆`Engine`中的java文件 排除混淆

#### 排除混淆

- 提供给JS调用的接口,请在方法添加`@AppCanAPI`注解，混淆时会排除
- 提供给插件调用或不希望被混淆的，请添加`@Kepp`注解，混淆时会排除



### AppCan Gradle插件

一般情况下生成引擎只需要调用`gradle buildEngine` 就可以生成所有的引擎包，如果有其他需要可以调用其他的task。目前提供的task有：

- **build{Flavor}Engine**：生成对应flavor的引擎

  如输出crosswalk引擎

  ```sh
  gradle buildCrosswalkEngine
  ```

  输出所有flavor的引擎

  ```sh
  gradle buildEngine
  ```

- **jar{Flavor}Engine**:生成对应flavor的jar，不进行混淆

  如输出crosswalk引擎的jar

  ```sh
  gradle jarCrosswalkEngine
  ```

  如果要输出所有flavor的jar

  ```sh
  gradle jarEngine
  ```

- **proguard{Flavor}Engine**：生成对应flavor的jar，并且进行混淆

  如输出混淆过的crosswalk引擎jar

  ```sh
  gradle proguardCrosswalkEngine
  ```

  如果要输出所有flavor的jar

  ```sh
  gradle proguardEngine
  ```

  ​



###  
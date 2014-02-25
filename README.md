Garigarimaru_main
======================

プロジェクト名称：激ゃせガリガリ丸  
プロジェクト概要：体重管理ソフト  
開発基盤：Android  
開発言語：Java+SQL  
開発者：大竹 洋平  
バージョン：1.0

動作条件
------

+ Andoroid端末（Nexus7）
+ Android仮想デバイス（AVD）
+ ソフトウェアキーボード


インストール
----------------

1. Eclipseの起動  
2. プロジェクトのインポート  
2.1 画面左のファイルエクスプローラーで右クリック  
　　コンテキストメニュー -> Android -> ”Existing Android Code Into Workspace” -> "次へ"をクリック  
2.2 ”Root Directory”の右の参照ボタンをクリックし、"Garigarimaru_main”を選択しOKボタンをクリック  
2.3 ”プロジェクトをワークスペースにコピー”にチェックをつける  
2.4 完了ボタンをクリック

3. アプリケーションの起動  
3.1 実行したいプロジェクトをアクティブにする  
　　ファイルエクスプローラー -> Garigarimaru_main をクリック  
3.2 実行ボタンをクリック  
3.3 アプリケーションが起動するまで待機する  
　　（エミュレーター -> Android -> アプリ の順に自動的に起動する）

**Note**  

+ 実行ボタンはタスクバーに存在する緑色のアイコン  
+ メニューバー -> 実行”-> 実行（R）でも実行可能  
+ 仮想デバイスの初回起動時に実行方法を尋ねられたら”Androidアプリケーション”を選択  
+ 仮想デバイスの初回起動時のLogCatの問いに許可で応じる  
+ ハードウェアキーボードでは正常に動作しないので、ソフトウェアキーボードを使用する  


機能
--------
### 体重管理ソフト
入力した身長と体重からBMIを計算し、適性体重になるまでに消費すべきカロリーと歩数を計算しグラフにして表示することが主な機能。  


### 機能一覧

    01. 身長と体重の入力（データベースに保存）
    02. 不正な入力値のチェックと再入力要求
    03. 履歴の読み込み（データベースから読み込み）
    04. テーブルの表示（データベースから読み込み）
    05. 適正体重、BMI、消費すべきカロリーの計算
    06. グラフ表示（データベースから読み込み）
    07. 閲覧情報の読み込み（閲覧中の履歴情報の画面間共有）
    08. コメントの出力（達成度、継続度、努力度から出力パターンを変化）
    09. リセット機能（新規減量開始、データベースのテーブルにスタック）
    10. 削除機能（全ての履歴を消去、データベースの全テーブルの破棄）
    11. バックグラウンド処理（加速度センサーの起動と終了）
    12. 万歩計（歩数のカウント）


その他
----------
+ 実装担当は3人いるが、ここでは自分が実装した箇所のみ掲載しているため一部未完成の箇所がある
+ 仕様書では減量期間中の体重の増加を想定していない
+ テーブルとグラフはスライドスクロールすることが可能

 
コピーライト
----------
Copyright &copy; 2014 Yohei_Otale All rights reserved.

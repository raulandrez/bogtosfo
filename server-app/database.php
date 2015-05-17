<?php
class point{
    private $pg;
    function connectar(){
        require_once 'config.inc.php';
          $this->pg = pg_connect("host='$pg_serverver' port='$pg_port' dbname='$pg_database' user='$pg_user' password='$pg_password'");	
        if( !$this->pg){
            echo "<h1>porfavor revisar la Configuracion de la Base de Datos</h1>";
            die();
        }
    }
    function desconectar(){
        pg_close ($this->pg);
    }
    function point($track_id,$x,$y,$z,$t){
        $this->connectar();
        $sql="INSERT INTO point( x, y, z, t, track_id)  VALUES ( $x, $y, $z, $t, $track_id);";
        pg_query ($this->pg,$sql);
        $this->desconectar();
    }

};
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


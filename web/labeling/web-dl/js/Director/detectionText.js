//
/**
 * @author zhaiyunpeng
 * @copyright 2019
 * javascript for person and vehicle detection
 */

var regions;

var ip = getIp();
var token = getCookieOrMessage("token");
var userType = getCookieOrMessage("userType");
console.log("token=" + token);
var loadFinished=false; //判断是否加载完成，防止上下张快捷键过快，导致保存冲刷掉原有数据，保存空数据
var textContent_div;

if(userType == 2){
  $('#title_id').text("通用文本审核");
}


function getCookieOrMessage(key){
	var value = getCookie(key);
	
	return value;
}

function getSessionStorageMessage(key){
	var value = sessionStorage.getItem(key);
	if(isEmpty(value)){
		return localStorage.getItem(key);
	}
	return value;
}


//获取元素绝对位置 获取鼠标坐标
function getElementLeft(element){
    var actualLeft = element.offsetLeft;
    var current = element.offsetParent;
    while (current !==null){
        actualLeft += (current.offsetLeft+current.clientLeft);
        current = current.offsetParent;
    }
    return actualLeft;
}

function getElementTop(element){
    var actualTop = element.offsetTop;
    var current = element.offsetParent;
    while (current !== null){
        actualTop += (current.offsetTop+current.clientTop);
        current = current.offsetParent;
    }
    return actualTop;
}


//
var verifyMap = {"1":"大小不合格","2":"颜色不合格","3":"其它不合格"};
var label_task = getSessionStorageMessage("label_task");
var label_task_status = getSessionStorageMessage("label_task_status");  //1表示审核
console.log("label_task=" + label_task);
var label_task_info;
var labeltastresult;

var rects=[];


var undoShape;

var color_dict = {"rect":"#13c90c","car":"#0099CC", "person":"#FF99CC","point":"#00cc00","pointselected":"red"};
var color_person = {"0":"#13c90c","1":"#fc0707","2":"#FF99CC","3":"#fceb07"};
var color_all={"0":"#13c90c","1":"#fc0707","2":"#FF99CC","3":"#fceb07","4":"#FF33FF","5":"#666600","6":"#4B0082","7":"#B8860B","8": "#000000","9":"#800000","10": "#FF00FF","11":"#8B4513","12":" #0000CD","13":" #008B8B","14":"#708090","15": "#00FFFF","16":"#FF00FF","17":"#9370DB","18":"#7CFC00","19":"#F08080","20":"#C71585"};


var fileindex=0;
var lastindex=false;

var isLabelChange = false;
var maxIdNum=0;

function textObj(textStr,type,dataPos){
	this.textStr = textStr;
	
	this.isSelected=false;
	this.id="";
	this.dataPos = dataPos;
	this.length = textStr.length;
	this.other = {};//自定义属性 
    this.other['region_attributes'] = {};
	this.setType = function(type){
		this.other['region_attributes']['type'] = type;
	};
	this.getType = function(){
		if(isStrEmpty(this.other['region_attributes']['type'])){
			return "-1";
		}else{
			return this.other['region_attributes']['type'];
		}
	}
	this.setType(type);
}

function isStrEmpty(str){
    if(typeof str == "undefined" || str == null){
       return true;
    }
    return false;
}

function reset_var(){
   rects=[];
   loadFinished = false;
}

var label_attributes ;

//by yunpeng zhai
//基本对象的定义 点， 矩形， 多边形MASK
//




function updateSetting(){
  hidePopup();
  // show_region_attributes_update_panel();
  var set_attributes = document.getElementById("set_attributes")
  set_attributes.setAttribute('style', 'top:' + 185 + 'px;left:'+ 50 +'px;width:'+ 502+'px;position:absolute');
  update_attributes_update_panel();
  $('#message_panel').css('display','block');
  $('#message_panel .content').css('display','inline');
}


function save_attribute(){
  var set_attributes = document.getElementById("set_attributes");

  var _via_attributes_str = JSON.stringify(_via_attributes['region']);
  update_labeltask(_via_attributes_str);

  set_attributes.style.display = "none";
  document.getElementById('message_panel').style.display = "none";
  document.getElementById('message_panel_content').style.display = 'none';
}

function close_attribute(){
   set_attributes.style.display = "none";
   onload();
   document.getElementById("user_input_attribute_id").value='';
   document.getElementById('attribute_properties').innerHTML = '';
   document.getElementById('attribute_options').innerHTML = '';
   document.getElementById('message_panel').style.display = "none";
   document.getElementById('message_panel_content').style.display = 'none';
}


function close_exist_child_attributes(){
  var set_attributes = document.getElementById("atttibute_child");
  document.getElementById('atttibute_childe').innerHTML = '';
}

function deleteAllRect(){
	rects=[];
	var rect = new textObj(textContent,"-1",0);
	rects.push(rect);
    refreshLabel();
}


function getDefaultType(){
	var tmpType = "";
	if(!isEmpty(_via_attributes['region']['type']) && !isEmpty(_via_attributes['region']['type']["default_options"])){
		 if(_via_attributes['region']['type']['type'] == "dropdown" || _via_attributes['region']['type']['type'] == "radio" || _via_attributes['region']['type']['type'] == "checkbox"){
			  var tmp = _via_attributes['region']['type']["default_options"];
			  console.log("aa=" + tmp);
			  for(var key in tmp){
			     if(tmp[key] == true){
                   tmpType = key;
                   break;				 
			     }				
		      }
		 }
	 }
	 console.log("defaulttyype=" + tmpType);
	 return tmpType;
}

var textContent;
function getTextContent(filename){
	$.ajax({
       type:"GET",
       url:ip + "/api/gettextcontent",
       headers: {
          authorization:token,
       },
       dataType:"text",
       data:{
       	'filename':filename
		},
       async:false,
       success:function(json){
		   textContent = json;
		   console.log(textContent);
        },
	    error:function(response) {
		   console.log(response);
        }
   });
}

function showText(){
	var html = "";
	for (var j=0;j<rects.length;j++){
		var rect = rects[j];
		console.log("j=" + j + " type=" + rect.getType() + " datapos=" + rect.dataPos);
		
		if(rect.getType() == "-1"){
			html += "<span data-v-292bb175=\"\" data-v-c66bed24=\"\" class=\"textsplan\" data-position=\"" + rect.dataPos + "\">" + rect.textStr + "</span>"
		}else{
			html +=
			"<span data-v-292bb175=\"\" class=\"highlight bottom\" style=\"border-color: rgb(230, 209, 118);\">" + 
			"<span data-v-292bb175=\"\" class=\"highlight__content\" data-position=\"" + rect.dataPos + "\">" + rect.textStr + "<button data-v-292bb175=\"\" type=\"button\" class=\"v-icon notranslate delete v-icon--link mdi mdi-close-circle theme--light\" onclick=\"deleteLabel(" + rect.id + ")\"></button></span><span data-v-292bb175=\"\" data-label=\"" + rect.getType() + "\" class=\"highlight__label\" style=\"background-color: rgb(230, 209, 118); color: rgb(0, 0, 0);\"></span></span>";
		}
	}
	console.log(html);
	return html;
}

function refreshLabel(){
	document.getElementById("textContent_div").innerHTML = showText();
}

function loadText(){
  reset_var();
  regions = rects;
  var textPath = labeltastresult[fileindex].pic_image_field;
  console.log("text:" + textPath);
  getTextContent(textPath);
  
  parse_labelinfo(labeltastresult[fileindex].label_info);

  refreshLabel();
  
  var fname = textPath.substring(textPath.lastIndexOf("/") + 1);
  if(!isEmpty(labeltastresult[fileindex].pic_real_name)){
	 fname = labeltastresult[fileindex].pic_real_name.substring(labeltastresult[fileindex].pic_real_name.lastIndexOf('/') + 1);
  }
  html ="文件名：" + fname + ", 第" + (tablePageData.current * pageSize + fileindex + 1) + "个文件，共" + tablePageData.total +  "个文件。"
  document.getElementById("float_text").innerHTML = html;
  loadFinished = true;
}

function deleteLabel(id){
  for(var i = 0; i < rects.length;i++){
	 if(rects[i].id == id){
		rects.splice(i,1);
		break;
	 }
  }
  refreshLabel();
}

function showPopup(topx,lefty) {
  
  _via_display_area_content_name = VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE;
  _via_is_region_selected = true;
  annotation_editor_show(topx,lefty,"","");
}

function hidePopup() {
  console.log("hide popup");
  annotation_editor_remove();
}

function selectText(e){
	
	if(!e){
		e = window.event;
	}
	//console.log("x=" + e.pageX + " y=" + e.pageY);
	
	var clickX = e.pageX - getElementLeft(textContent_div);
    var clickY = e.pageY - getElementTop(textContent_div);
	
	//console.log("clickX=" + clickX + " clickY=" + clickY);

	var selection = window.getSelection();
	
    var selectionStr = selection.toString();
	console.log("select text:" + selectionStr);
	var initType = "-1";
	
    if(selectionStr.length > 0) {
		var selectAnchorNode = selection.anchorNode;
        var selectFocusNode = selection.focusNode;
		if(selectAnchorNode != selectFocusNode){
			console.log("start node not equal end node. no need to deal. return.");
			return;
		}
		if(isEmpty(selectAnchorNode.parentElement.getAttribute("data-position"))){
			console.log("the attribute data-position is null. so return.");
			return;
		}
		var dataPos = parseInt(selectAnchorNode.parentElement.getAttribute("data-position"));
		var start = selection.anchorOffset;
        var end = selection.focusOffset;
		console.log("start=" + start + " end=" + end);
		var userSetType="";

		var setDefaultValueIndex = -1;
		for(var i = 0; i < rects.length;i++){
			if(rects[i].dataPos == dataPos){
				maxIdNum = maxIdNum + 1;
				var findRect = rects[i];
				if(start == 0){
					setDefaultValueIndex = i;
					if(end == findRect.length){
						//整段变更
						findRect.setType(userSetType);
						findRect.id=maxIdNum;
					}else{
						//拆分成两段
						findRect.setType(userSetType);
						
						var rect = new textObj(findRect.textStr.substring(end),initType,findRect.dataPos + end);
						rect.id = maxIdNum;
						
						rects.splice(i+1,0,rect); 
						findRect.textStr =findRect.textStr.substring(0,end);
					}
				}else{
					setDefaultValueIndex = i + 1;
					if(end == findRect.length){
						//拆分成两段
						var rect = new textObj(findRect.textStr.substring(start),userSetType,findRect.dataPos + start);
						rect.id = maxIdNum;
						rects.splice(i+1,0,rect);
						findRect.textStr =findRect.textStr.substring(0,start);			
					}else{
						//拆分成三段
						var rect1 = new textObj(findRect.textStr.substring(start,end),userSetType,findRect.dataPos + start);
						rect1.id = maxIdNum;
						
						var rect2 = new textObj(findRect.textStr.substring(end),initType,findRect.dataPos + end);	
						
						rects.splice(i+1,0,rect1,rect2);	
						findRect.textStr =findRect.textStr.substring(0,start);
					}
				}
				
				break;
			}
		}
		refreshLabel();
		
		select_only_region(setDefaultValueIndex);
		showPopup(clickY,clickX);
    }else{
		var objEle = e.target || e.srcElement;
		var dataPosition = objEle.getAttribute("data-position");
		console.log("dataPosition=" + dataPosition);
		var findIndex = -1;
		for(var i = 0; i < rects.length;i++){
			if(initType == rects[i].getType()){
				continue;
			}
			if(rects[i].dataPos == dataPosition){
				findIndex = i;
				break;
			}
		}
		if(findIndex != -1){
			select_only_region(findIndex);
			showPopup(clickY,clickX);
		}
	}
	loadFinished = true;
 }


function save(){
  var re = updatelabel(fileindex);
  if (re=true){
    //window.alert("保存成功!");
  }
  else{window.alert("保存失败!");}
  showfilelist();
}


function clearCache(){
  rects = [];
  masks = [];
  pointShapes = [];
}

function next(){
  currentImage = false;
  if(fileindex<labeltastresult.length-1)  {
    if(loadFinished==true){
        updatelabel(fileindex);
    }
    fileindex=fileindex+1;	 
    loadText();
    showfilelist();
  }else{
	  if((tablePageData.current + 1) * pageSize < tablePageData.total){
           if (loadFinished==true){
		      updatelabel(fileindex);
            }
		   nextPage();
	  }
  }
  loadFinished=false;
}

function last(){  
  currentImage = false;  
  if(fileindex>0){
      if(loadFinished==true){
         loadFinished=false;   
         updatelabel(fileindex);
      }
	  fileindex=fileindex-1;
	  loadText();
      showfilelist();  
  }else{
	  var current = $('#displayPage1').text();
	  if(current > 1){
	     lastindex = true;
         if(loadFinished==true){
            loadFinished=false;   
		    updatelabel(fileindex);
         };
         prePage();
	     lastindex = false;
	  }
  }  
}


function clickfilelist(index){
  fileindex = index;
  loadText();
  showfilelist();
  currentImage = false;
}


function setVerifyText(rect,x,y){
	if(!isEmpty(rect.other.region_attributes.verify)){
		if(rect.other.region_attributes.verify != 0){
			context.font = "15px Georgia";
			context.fillStyle= context.strokeStyle;
			context.fillText(verifyMap[rect.other.region_attributes.verify], x,y-5);
		}
	}
}
	

function setVerifyLineWidth(rect){
	if(!isEmpty(rect.other.region_attributes.verify)){
		if(rect.other.region_attributes.verify != 0){
			context.lineWidth = 3;
			context.strokeStyle = "#DC143C";
		}
	}
}

function selectColor(regions,i){
	if(!isEmpty(_via_attributes['region']['type'])){
		var dict = _via_attributes['region']['type']["options"];
		var color_num=0;
		for (var key in dict){
			if (key==regions[i].other.region_attributes.type){
			   context.strokeStyle=color_all[String(color_num)]; 
			   break;
			}
			color_num++;
		}
	}
}



function updateLabelHtml(){
	 document.getElementById("boxlabels").innerHTML=boxlabelshtml();
     document.getElementById("labelcounttable").innerHTML=boxlabelcounthtml();
}


//边栏
//任务信息
function showtaskinfo(){

  $('#task_info').text(label_task_info.task_name);
  $('#task_progress').text(label_task_info.task_status_desc);
  
}

var orderType = 1;
var findLast = 0;

function  showOrder(value){
  hidePopup();
  if (value == "1"){ //文件名排序
     orderType = 1;
     findLast  = 0;
  }
  else if (value == "2"){
    orderType = 0;
    findLast  = 0;
  }
  else if (value == "3"){
    orderType = 0;
    findLast  = 3;
  }
  page(0,pageSize);  
}

function skipLast(){
  hidePopup();
  if (orderType == 0){
    return ;
  }
    findLast  = 1;
    page(0,pageSize); 

}

function isVerified(){
	if(userType == 2 && label_task_status == 1){
		return true;
	}
	return false;
}

//显示文件列表
function showfilelist(){
    var htmlstr="";
    for (var i=0;i<labeltastresult.length;i++){
       var fname = labeltastresult[i].pic_image_field.substring(labeltastresult[i].pic_image_field.lastIndexOf('/') + 1);
	   if(!isEmpty(labeltastresult[i].pic_real_name)){
		  fname = labeltastresult[i].pic_real_name.substring(labeltastresult[i].pic_real_name.lastIndexOf('/') + 1);
	   }
       var isfinished = labeltastresult[i].label_status;
	   if(isVerified()){
		   isfinished = labeltastresult[i].verify_status - 1;
	   }
       var lablebg=" style=\"cursor:pointer\"";
       var finish="未完成";
       if (isfinished=="0"){finish="已完成";}
       if (i==fileindex){lablebg=" style=\"background:#eee;color:#5a5a5a;cursor:pointer;\"";}
	   var classStr = "";
	   if (isfinished=="0"){
		   // classStr = " class=\"btn btn-xs btn-success\"";
       classStr = "class=\"file-select\"";//
	   }
	   if(isVerified()){
		   htmlstr = htmlstr+"<tr onclick=\"clickfilelist("+i+");\""+ lablebg+"> <td width=\"70\"" +"style=\"vertical-align:middle\""+ classStr + ">"+"<button"+classStr+" type=\"button\" onclick=\"changeVerifyStatus("+i+");\" style=\"border:none;background:none\">"+finish +"</button>"+"</td><td>"+ fname+ "</td></tr>"; 
	   }else{
	       htmlstr = htmlstr+"<tr onclick=\"clickfilelist("+i+");\""+ lablebg+"> <td width=\"70\"" +"style=\"vertical-align:middle\""+ classStr + ">"+"<button"+classStr+" type=\"button\" onclick=\"changeStatus("+i+");\" style=\"border:none;background:none\">"+finish +"</button>"+"</td><td>"+ fname+ "</td></tr>";
	   }
       
    };
    document.getElementById("filelist").innerHTML=htmlstr;
}

function changeStatus(i){
   var tmpId='changeStatus_'+i;
  var htmlstr= "<div class=\"panel\" style=background:#ffff;border: 1px solid rgba(0,0,0,0.2);border-radius: 6px;top:10px;"
     +">"
     +"<label for=\"st\" style=\"margin:15px;color:#333\"> 标注状态修改为:</label>"+
      "<select id="+tmpId+" name=\"标注状态\"  style=\"text-align:center;border-radius:4px;\">"+
      "<option value=0 selected=\"\">已完成</option>"+
      "<option value=1 >未完成</option>"+
      "</select>"+
      "<br />"+
      "<button onclick=\"updateStatus("+i+");\" class=\"btn btn-default\" style=\"margin:0  auto;display: block;border-radius:5px;width: 50px;\">提交</button>"
    +"</div>"

   document.getElementById("labelStatus").innerHTML=htmlstr;
}



function changeVerifyStatus(i){
   var tmpId='changeStatus_'+i;
  var htmlstr= "<div class=\"panel\" style=background:#ffff;border: 1px solid rgba(0,0,0,0.2);border-radius: 6px;top:10px;"
     +">"
     +"<label for=\"st\" style=\"margin:15px;color:#333\"> 审核状态修改为:</label>"+
      "<select id="+tmpId+" name=\"审核状态\"  style=\"width:100px;text-align:center;border-radius:4px;\">"+
      "<option value=1 selected=\"\">已完成</option>"+
      "<option value=0 >未完成</option>"+
      "</select>"+
      "<br />"+
      "<button onclick=\"updateVerifyStatus("+i+");\" class=\"btn btn-default\" style=\"margin:0  auto;display: block;border-radius:5px;width: 50px;\">提交</button>"
    +"</div>"

   document.getElementById("labelStatus").innerHTML=htmlstr;
}

function updateStatus(i){
  var status_id = 'changeStatus_'+i;
  var label_status=document.getElementById(status_id).value;
  labeltastresult[fileindex].label_status = label_status;
  $.ajax({
         type:"PATCH",
         url:ip + "/api/label-task-item-status/",
         contentType:'application/json',
         headers: {
            authorization:token,
          },
         dataType:"json",
         async:false,
         data:JSON.stringify({'id':labeltastresult[fileindex].id,
							 'label_task_id':label_task_info.id,
                              'label_status':label_status,
         }),
         success:function(json){
          showfilelist();
          document.getElementById("labelStatus").innerHTML = "";
          return true;
        },
	    error:function(response) {
		  redirect(response);
        }
   });


}

function updateVerifyStatus(i){
  var status_id = 'changeStatus_'+i;
  var verify_status=document.getElementById(status_id).value;
  labeltastresult[fileindex].verify_status = verify_status;
  $.ajax({
         type:"PATCH",
         url:ip + "/api/label-task-item-verify-status/",
         contentType:'application/json',
         headers: {
            authorization:token,
          },
         dataType:"json",
         async:false,
         data:JSON.stringify({'id':labeltastresult[fileindex].id,
							'label_task_id':label_task_info.id,
                           'verify_status':verify_status,
         }),
      
         success:function(json){
           showfilelist();
           document.getElementById("labelStatus").innerHTML = "";
           return true;
         },
	     error:function(response) {
		  redirect(response);
         }
   });

}


function parse_labelinfo(labelinfo){
	rects.length = 0;
	if(!isEmpty(labelinfo)){
		// console.log("标注信息："+labelinfo);
		var label_arr = JSON.parse(labelinfo);
		var lastDataPos = 0;
		for(var i=0;i<label_arr.length;i++){
			if(label_arr[i].dataPos > lastDataPos){
				var rect = new textObj(textContent.substring(lastDataPos,label_arr[i].dataPos),"-1",lastDataPos);
				rects.push(rect);
			}
			
			var rect = new textObj(label_arr[i].textStr,label_arr[i].class_name,label_arr[i].dataPos);
			rect.other = label_arr[i].other;
			rect.length = label_arr[i].length;
			rect.id = label_arr[i].id;
		    rects.push(rect);
			lastDataPos = label_arr[i].dataPos + label_arr[i].length;
	    }
		if(lastDataPos != textContent.length){
			var rect = new textObj(textContent.substring(lastDataPos),"-1",lastDataPos);
			rects.push(rect);
		}
	}else{
		var rect = new textObj(textContent,"-1",0);
		rects.push(rect);
	}
    // 加载值：需要打开已有属性和属性值，需要回传_via_attributes， _via_img_metadata两个值的获取
    //暂时默认设置只有类别属性
    set_display_area_content( VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE );
} 


function boxlabelCount(labelMap,shape){
	
	 var tmpType = shape.type;
	 
	 if(!isEmpty(shape.other["region_attributes"]["type"])){
		 tmpType = shape.other["region_attributes"]["type"];
	 }
	
	 if(labelMap[tmpType] == null){
        labelMap[tmpType] = 1;
     }else{
        labelMap[tmpType] = labelMap[tmpType] + 1;
     }
}

function boxlabelcounthtml(){
  var htmlstr="";
  var labelMap = {};
  for (var i=0;i<rects.length;i++){
	 boxlabelCount(labelMap,rects[i]);
  }
  for (var i=0;i<masks.length;i++){
	 boxlabelCount(labelMap,masks[i]);
  }
  for (var i=0;i<pointShapes.length;i++){
	 boxlabelCount(labelMap,pointShapes[i]);
  }
  for (item in labelMap){
    htmlstr = htmlstr + "<tr><td style=\"width:10px\"></td> <td>"+ item + "</td><td>" + labelMap[item] +"</td></tr>";
  }
  return htmlstr;
}


function submit_deletelabel(){
	
	var start_id = $('#delete_startid').val();
	var end_id = $('#delete_endid').val();
	if(start_id < 1 || start_id > tablePageData.total){
			alert("请输入正确的起始文件ID，范围为1到" + tablePageData.total);
			return;
	}
		
	if(end_id < 1 || end_id > tablePageData.total){
			alert("请输入正确的结束文件ID，范围为1到" + tablePageData.total);
			return;
	}
    //var one_reid_name =  $('#one_reid_name').val();
    $.ajax({
         type:"POST",
         url:ip + "/api/label-task-delete-label",
         headers: {
            authorization:token,
          },
         dataType:"json",
         async:false,
         data:{'label_task_id':label_task_info.id,
               'start_id':start_id,
               'end_id' : end_id
			   //'one_reid_name':one_reid_name
               },        
         success:function(res){
             console.log('创建数据信息');
         },
	     error:function(response) {
		    redirect(response);
         }
         });
	
	$("#deleteLabel").modal('hide');
	var current = $('#displayPage1').text();

    if(current >= 1){
	   pageReload(current - 1,pageSize,fileindex);
	}
	
}


function updatelabel(fileindex){
  var label_list=[]
  for (var i=0;i<rects.length;i++){
	  if(rects[i].getType() == -1){
		  continue;
	  }
	  var label= {'class_name':rects[i].getType(), "score":1.0};
	  if(isEmpty(rects[i].id)){
	     label['id'] = i+'';
	  }else{
		 label['id'] = rects[i].id+''; 
	  }
      label['textStr'] = rects[i].textStr;
	  label['dataPos'] = rects[i].dataPos;
	  label['length'] = rects[i].length;
      label['other']=rects[i].other;

      label_list.push(label);
  }

  labelinfo_jsonstr = JSON.stringify(label_list);
  var label_status=1;
  if(label_list.length > 0){ 
      label_status=0;
  }
  $.ajax({
       type:"PATCH",
       url:ip + "/api/label-task-item/",
	   contentType:'application/json',
       headers: {
          authorization:token,
        },
       dataType:"json",
       async:false,
	   data:JSON.stringify({'id':labeltastresult[fileindex].id,
							'label_task_id':label_task_info.id,
                            'label_info':labelinfo_jsonstr,
                            'label_status': label_status
        }),
       success:function(json){
		   return true;
	   },
	   error:function(response) {
		  redirect(response);
       }
   });
   labeltastresult[fileindex].label_info = labelinfo_jsonstr;
   labeltastresult[fileindex].label_status = label_status;
  
}

function get_labeltask(){
	console.log('query labeltask info.');
    $.ajax({
       type:"GET",
       url:ip + "/api/label-task/"+label_task+"/",
       headers: {
          authorization:token,
        },
       dataType:"json",
       async:false,
       success:function(json){
         label_task_info = json;
		 console.log('return labeltask info.' + label_task_info);
		 page(0,pageSize);
       },
	   error:function(response) {
		  redirect(response);
       }
   });
}


  function update_labeltask(task_label_type_info){
	  console.log("label_task_id=" + label_task_info.id);
	  console.log("task_label_type_info=" + task_label_type_info);
	  
      $.ajax({
         type:"PATCH",
         url:ip + "/api/label-task/",
         headers: {
            authorization:token,
          },
         dataType:"json",
         data:{
           'label_task_id':label_task_info.id,
           'task_label_type_info':task_label_type_info,
         },
         async:false,
         success:function(json){
           console.log(json);
         },
	     error:function(response) {
		  redirect(response);
         }
     });
  }


function list(current,pageSize,index=0){
    $.ajax({
       type:"GET",
       url:ip + "/api/label-task-item-page/",
       headers: {
          authorization:token,
        },
       dataType:"json",
	   data:{
		   'label_task':label_task_info.id,
		   'startPage':current,
		   'pageSize':pageSize,
		   'orderType': orderType,
           'findLast':findLast,
	   },
       async:false,
       success:function(json){
			tablePageData = json;
			tableData = json.data;
			labeltastresult = tableData;
			fileindex=index;
			if(lastindex){
				fileindex = pageSize - 1;
			}
        //console.log(json);
        // return json.token;
        },
	    error:function(response) {
		  redirect(response);
        }
   });
}

var pageSize = 20;
var tableData;
var tablePageData;

function pageReload(current,pageSize,fileindex){
  list(current,pageSize,fileindex);
  loadText();
  showtaskinfo();
  showfilelist();
	  
  //display_list();
  setPage(tablePageData,pageSize);
}

function page(current,pageSize){
  list(current,pageSize);
  loadText();
  showtaskinfo();
  showfilelist();
	  
  //display_list();
  setPage(tablePageData,pageSize);
}

function nextPage(){
   hidePopup();
   var current = $('#displayPage1').text();
   console.log("current=" + current);
   findLast = 0;
   page(current,pageSize);
}

function prePage(){
  hidePopup();
  var current =$('#displayPage1').text();
  console.log("current=" + current);
  if(current > 1){
    console.log("current=" + (current - 2));
    findLast = 0;
    page(current - 2,pageSize);
  } 
}

function goPage(){
   hidePopup();
   var goNum = $('#goNum').val();
    findLast = 0;
    var pageTotal = $("#totalNum").text();
    var pageNum = parseInt(pageTotal/pageSize);
    if(pageTotal%pageSize!=0){
        pageNum += 1;
    }else {
        pageNum = pageNum;
    }
    if (goNum<=0){
      alert("请输入大于0的数值");
    }
    else if(goNum<=pageNum){
        page(goNum - 1,pageSize);
    }
    else{
        alert("不能超出总页码！");
    }
}

$("#goNum").keydown(function (e) {
    if (e.keyCode == 13) {
        goPage();
    }
});

function setPage(pageData,pageSize){
  var startIndex = pageData.current * pageSize;
  if(startIndex < 10){
	  $('#startIndex').text(" " + (pageData.current * pageSize + 1));
  }else{
	  $('#startIndex').text(pageData.current * pageSize + 1);
  }
  var endIndex = pageData.current * pageSize + pageData.data.length;
  if(endIndex < 10){
	   $('#endIndex').text(" " + (pageData.current * pageSize + pageData.data.length));
  }else{
	   $('#endIndex').text(pageData.current * pageSize + pageData.data.length);
  }
 
  $('#totalNum').text(pageData.total);
  $('#displayPage1').text(pageData.current + 1);

  console.log("set prePage status, pageData.current=" + pageData.current);

  if(pageData.current == 0){
    console.log("set prePage disabled.");
    $('#prePage').removeAttr("href");
  }
  else{
    $('#prePage').attr("href","javascript:prePage()");
  }

  if((pageData.current + 1) * pageSize >= pageData.total){
    console.log("set nextPage disabled.");
    $('#nextPage').removeAttr("href");
  }
  else{
    $('#nextPage').attr("href","javascript:nextPage()");
  }

  var pageTotal = pageData.total;
  var pageNum = parseInt(pageTotal/pageSize);
  if(pageTotal%pageSize!=0){
      pageNum += 1;
  }else {
      pageNum = pageNum;
  }
  $("#totalPageNum").text(pageNum);
  
}



 window.onload = function() {

	var token = getCookieOrMessage("token");
	console.log("page load token=" + token);
    if(!isEmpty(token)){
		console.log("this is not null.token =" + token);
		doLoad();
	}
  };
  
function doLoad(){
	token = getCookieOrMessage("token");
    userType = getCookieOrMessage("userType");
	
	var medical_flag = getSessionStorageMessage("medical_flag");
	if(!isEmpty(medical_flag)){
	   $('.left-side').toggleClass("collapse-left");
       $(".right-side").toggleClass("strech");
       document.getElementById("hiddenLeft").style.display="none";
	   document.getElementById("logotitle").style.display="none";
	}
	
    if(typeof token == "undefined" || token == null || token == ""){
        console.log("token=" + token);
		console.log("onload tasks/detect/index.html");
        window.location.href = "../../login.html";
    }else{
        var nickName = getCookieOrMessage("nickName");
        console.log("nickName=" + nickName);
        $("#userNickName").text(nickName);
        $("#userNickName_bar").text(nickName);
    }
	label_task = getSessionStorageMessage("label_task");
    label_task_status = getSessionStorageMessage("label_task_status");  //1表示审核
	
	textContent_div = document.getElementById("textContent_div");
	textContent_div.onmouseup = selectText;
	
	labelRegion_div = document.getElementById("labelRegion");
	labelRegion_div.onmousedown = hidePopup;
	//textContent_div.onclick = hidePopup;
	
    get_labeltask();	  
	_via_init();
    //加载保存好的属性结构,如果不存在，加载默认的类别信息
	if(!isEmpty(label_task_info.task_label_type_info)){
		label_attributes = JSON.parse(label_task_info.task_label_type_info);
	}
    if (isEmpty(label_attributes)){
        get_init_atrribute();
    }
    else{
      if (Object.keys(label_attributes).length == 0){
          get_init_atrribute();
      }
      else{
        _via_attributes["region"] = label_attributes;
      }
    }
	
}
  
function get_init_atrribute(){
       _via_attributes = {'region':{}};

       atti = "type"; // 属性名
       _via_attributes['region'][atti] = {};
       _via_attributes['region'][atti]["type"] = "dropdown";
       _via_attributes['region'][atti]["description"] = "";
       _via_attributes['region'][atti]["options"] = {};
       _via_attributes['region'][atti]["options"]["new"] = "news";
       _via_attributes['region'][atti]["options"]["finance"] = "finance and economics";
	   _via_attributes['region'][atti]["options"]["sport"] = "sport";
       _via_attributes['region'][atti]["default_options"] = {};
}
	


function export_attribute(){
    var url = ip + "/api/task-export-label-property/";
	var $iframe = $('<iframe />');
    var $form = $('<form  method="get" target="_self"/>');
	$form.attr('action', url); //设置get的url地址

	$form.append('<input type="hidden"  name="task_id" value="' + label_task + '" />');
	$form.append('<input type="hidden"  name="type" value="labeltask" />');
		
	$iframe.append($form);
	$(document.body).append($iframe);
	$form[0].submit();//提交表单
    $iframe.remove();//移除框架

}

function import_attribute(){
   
   $('#datasetModal').modal('show');

}	

function submit_import_property(){
	var jsonContent = $("#jsoninput").val();
	if(!isJSON(jsonContent)){
		alert("输入格式非法。");
		return;
	}
	$.ajax({
         type:"POST",
         url:ip + "/api/task-import-label-property/",
         headers: {
            authorization:token,
          },
         dataType:"json",
         data:{
           'jsonContent':jsonContent,
		   'taskType':"labeltask",
		   'taskId':label_task
         },
         async:false,
         success:function(json){
           console.log(json);
         },
	     error:function(response) {
		  redirect(response);
         }
     });
	$('#datasetModal').modal('hide');
	close_attribute();
}

function isJSON(str) {
    if (typeof str == 'string') {
        try {
            var obj=JSON.parse(str);
            if(typeof obj == 'object' && obj ){
                return true;
            }else{
                return false;
            }

        } catch(e) {
            console.log('error：'+str+'!!!'+e);
            return false;
        }
    }
    console.log('It is not a string!');
	return false;
}	
 
 
  
  document.onkeydown = function(e){
      switch(e.keyCode){
          case 37:
          case 38:
          case 39:
          case 40:
            e.preventDefault();
      }
  }
  document.onkeyup=function(e){  
      console.log(e.keyCode)
      console.log(window.event);
      e=e||window.event;  
      e.preventDefault(); 
	  
	  
	  obj = e.srcElement||e.target;
      if( obj != null && obj !=undefined ){
          if(obj.type == "textarea" || obj.type=='text'){
			//console.log("obj.type:"+obj.type);
            return ;
          }
       }
	  
      switch(e.keyCode){  
        case 87: //W
          // boxcls = classes;
          createRectLabel();
     
          break; 
        case 68:
          deleterect();
          break;
        case 27:
          cancel();
          break;
        case 83:
          save();
          break;
        case 81:
          last();
          break;
        case 69:
          next();
          break;
        case 90://u
          undo();
          break;
        case 67://c
          copyOneBox();
          break;
        case 86://v
          paste();
          break;
        case 37:
          moveLeftSinglePx();
          break;
        case 38:
          moveUpSinglePx();
          break;
        case 39:
          moveRightSinglePx();
          break;
        case 40:
          moveDownSinglePx();
          break;
      };
  }
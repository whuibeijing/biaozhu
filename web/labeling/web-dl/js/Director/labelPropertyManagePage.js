var token = getCookie("token");
var userType = getCookie("userType");
if(isEmpty(userType)){
	userType =1;
}
var ip = getIp();

var pageSize = 10;

var tableData;
var tablePageData;



var VIA_REGION_SHAPE = { RECT:'rect',
                         CIRCLE:'circle',
                         ELLIPSE:'ellipse',
                         POLYGON:'polygon',
                         POINT:'point',
                         POLYLINE:'polyline'
                       };


var VIA_ATTRIBUTE_TYPE = { TEXT:'text',
                           CHECKBOX:'checkbox',
                           RADIO:'radio',
                           DROPDOWN:'dropdown'
                         };                        

var VIA_DISPLAY_AREA_CONTENT_NAME = {IMAGE:'image_panel',
                                     IMAGE_GRID:'image_grid_panel',
                                     SETTINGS:'settings_panel',
                                     PAGE_404:'page_404',
                                     PAGE_GETTING_STARTED:'page_getting_started',
                                     PAGE_ABOUT:'page_about',
                                     PAGE_START_INFO:'page_start_info',
                                     PAGE_LICENSE:'page_license'
                                    };

var VIA_ANNOTATION_EDITOR_MODE    = {SINGLE_REGION:'single_region',
                                     ALL_REGIONS:'all_regions'};
var VIA_ANNOTATION_EDITOR_PLACEMENT = {NEAR_REGION:'NEAR_REGION',
                                       IMAGE_BOTTOM:'IMAGE_BOTTOM',
                                       DISABLE:'DISABLE'};

var regions;
var _via_attributes                    = { 'region':{}};
var _via_current_attribute_id          = '';
var _via_attribute_being_updated = 'region';
var _via_metadata_being_updated = 'region';
var _via_annotation_editor_mode     = VIA_ANNOTATION_EDITOR_MODE.SINGLE_REGION;
var _via_display_area_content_name          = ''


var tmpCurrent = sessionStorage.getItem("label_property_task_page");
if(isEmpty(tmpCurrent)){
	tmpCurrent = 0;
}

page(tmpCurrent,pageSize);

function page(current,pageSize){
  list(current,pageSize);
  display_list();
  setPage(tablePageData,pageSize);
  sessionStorage.setItem('label_property_task_page',current); 
}

function nextPage(){
   var current = $('#displayPage1').text();
   console.log("current=" + current);
   page(current,pageSize);
}

function prePage(){
  var current =$('#displayPage1').text();
  console.log("current=" + current);
  if(current > 1){
    console.log("current=" + (current - 2));
    page(current - 2,pageSize);
  } 
}


function goPage(){
   var goNum = $('#goNum').val();

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
  if (isEmpty(pageData)){
    return;
  }
   var startIndex = pageData.current * pageSize;
  if(pageData.total > 0){
	  startIndex = startIndex + 1;
  }
  $('#startIndex').text(startIndex);
  $('#endIndex').text(pageData.current * pageSize + pageData.data.length);
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





function list(current,pageSize){
      $.ajax({
       type:"GET",
       url:ip + "/api/label-property-task-page/",
       headers: {
          authorization:token,
        },
       dataType:"json",
       data:{'startPage':current,
             'pageSize':pageSize},
       async:false,
       success:function(json){
        tablePageData = json;
        tableData = json.data;
        //console.log(json);
      },
	    error:function(response) {
		  redirect(response);
        }
   });
}

function display_list(){

  var html="<tr>\
            <th></th>\
            <th id=\"labeltask_head\"></th>\
            <th>标注属性任务名称</th>\
			<th>标注属性</th>\
            <th>任务类型</th>\
			<th>创建者</th>\
            <th>创建时间</th>\
            <th>操作</th>\
            </tr>";
 for (var i=0;i<tableData.length;i++){
    var row = "<tr>\
            <td><input type=\"checkbox\" class=\"flat-grey list-child\"/></td>\
            <td id=\"labeltask_id\">"+tableData[i].id+"</td>\
            <td>"+tableData[i].task_name+"</td>\
            <td width=\"200\">"+tableData[i].propertyJson+"</td>\
            <td>"+ getTaskTypeDesc(tableData[i].task_type) +"</td>\
            <td>"+tableData[i].assign_user+"</td>\
            <td>"+tableData[i].task_add_time+"</td>\
            <td>" + getOper(i,tableData[i]) + "</td>\
            </tr>";

    html=html+row;
  }
  //console.log(html);
  document.getElementById('label_property_list').innerHTML=html;

  $('#label_property_list tr').find('td:eq(1)').hide();
  $('#label_property_list tr').find('th:eq(1)').hide();
}

function getOper(index,data){
	console.log("data=" + data.task_status);
	if(data.task_status == 1){
		return  "<a onclick=\"exportLabelProperty(\'"+ data.id + "\');\" class=\"btn btn-xs btn-success\">导出</a>" ;
	}else{
		return  "<a onclick=\"setLabelProperty("+index+");\" class=\"btn btn-xs btn-success\">编辑</a>&nbsp;&nbsp;&nbsp;" 
			+
			 "<a onclick=\"exportLabelProperty(\'"+data.id+"\');\" class=\"btn btn-xs btn-success\">导出</a>" ;
	}
}

function getTaskTypeDesc(task_type){
	if(task_type == 0){
		return "私有";
	}else{
		return "公开";
	}
}

function setLabelProperty(tabledata_index){
	
    _via_attributes                    = { 'region':{}};
	_via_attributes['region'] = JSON.parse(tableData[tabledata_index].propertyJson);
	_via_current_attribute_id          = '';
	_via_display_area_content_name

	document.getElementById('attribute_properties').innerHTML = '';
	document.getElementById('attribute_options').innerHTML = '';
	$('#labelpropertyname').val(tableData[tabledata_index].task_name);
	$('#task_type').val(tableData[tabledata_index].task_type);
	
	//$("#task_type option[text='选择B']").attr("selected", "selected");
	
	$('#hide_taskid').val(tableData[tabledata_index].id);
	$('#user_input_attribute_id').val("");
	
    update_attributes_update_panel();
    annotation_editor_update_content(regions);
	
	$("#LabelPropertyModal").modal('show');

}

function exportLabelProperty(taskid){
	var url = ip + "/api/task-export-label-property/";
	var $iframe = $('<iframe />');
    var $form = $('<form  method="get" target="_self"/>');
	$form.attr('action', url); //设置get的url地址

	$form.append('<input type="hidden"  name="task_id" value="' + taskid + '" />');
	$form.append('<input type="hidden"  name="type" value="labelpropertytask" />');
		
	$iframe.append($form);
	$(document.body).append($iframe);
	$form[0].submit();//提交表单
    $iframe.remove();//移除框架
}

function delete_LabelProperty(){
  var stop = del();
  if (stop){
    return;
  }
  var Check = $("table[id='label_property_list'] input[type=checkbox]:checked");//在table中找input下类型为checkbox属性为选中状态的数据
        Check.each(function () {//遍历
              var row = $(this).parent("td").parent("tr");//获取选中行
              var id = row.find("[id='labeltask_id']").html();//获取name='Sid'的值
              delete_labelpropertytask_byid(id);
          });
  page(0,pageSize);
}

function del(){
    if($("table[id='label_property_list'] input[type=checkbox]").is(":checked")) {
        if (confirm("确实要删除吗？")) {
            // alert("已经删除！");
            return false;
        } else {
            // alert("已经取消了删除操作");
            return true;
        }
    }else if($("table[id='label_property_list']").find("input").length=="0"){
        alert("暂无可删的数据！");
        return true;
    }else{
        alert("请先选择需要删除的选项！");
        return true;
    }
}

function delete_labelpropertytask_byid(property_task_id){
  $.ajax({
    type:"DELETE",
    url:ip + "/api/delete-label-property-task/",
    headers: {
       authorization:token,
     },
    dataType:"json",
    async:false,
    data:{'property_task_id': property_task_id},
    success:function(res){
      console.log(res);
	  if(res.code != 0){
			alert("删除失败，" + res.message); 
	  }
    },
	error:function(response) {
		  redirect(response);
    }
 });
}

function createLabelProperty(){
	$('#hide_taskid').val("");
	_via_attributes                    = { 'region':{}};
	_via_current_attribute_id          = '';
	_via_display_area_content_name
	_via_attributes["region"]["id"] = {"type":"text","description":"","default_value":""};
	_via_attributes["region"]["type"] = {"type":"dropdown","description":"","default_value":"","options":{"person":"person","car":"car"},"default_options":{"person":true}};
	document.getElementById('attribute_properties').innerHTML = '';
	document.getElementById('attribute_options').innerHTML = '';
	$('#labelpropertyname').val("");
	$('#user_input_attribute_id').val("");
	
    update_attributes_update_panel();
    annotation_editor_update_content(regions);
}

var createsucced;
function submit_labelpropertytask(){
	var task_type = $('#task_type option:selected').val();
	var task_name = $('#labelpropertyname').val();
	var propertyJson = JSON.stringify(_via_attributes['region']);
	
	if (isEmpty(task_name) || task_name.length > 32){
       alert("任务名称不能为空或者不能超过32个字符。");
       return;
    }
	
	if (isEmpty(propertyJson)){
       alert("标注属性不能为空。");
       return;
    }
	createsucced = true;
	var task_id = $('#hide_taskid').val();
	if(isEmpty(task_id)){
		 create_labelpropertytask(task_type, task_name, propertyJson);
	}else{
	     update_labelpropertytask(task_id,task_type, task_name, propertyJson);
	}
	
    if(createsucced){
	  $("#LabelPropertyModal").modal('hide');
    }
    page(0,pageSize);
}


function update_labelpropertytask(task_id, task_type,task_name,propertyJson){
	$.ajax({
       type:"POST",
       contentType:'application/json',
       url:ip + "/api/update-label-property-task/",
       dataType:"json",
       async:false,
       headers: {
          authorization:token,
        },
       data:JSON.stringify({'task_name':task_name,
                            'task_type':task_type,
                            'propertyJson':propertyJson,
							"id":task_id
       }),
       success:function(res){
		  console.log(res);
		  if(res.code == 0){
			 createsucced = true;
		  }
		  else{
			 createsucced = false;
		  }
		},
	    error:function(response) {
		  redirect(response);
		  createsucced = false;
        }
   });
	
}

function create_labelpropertytask(task_type,task_name,propertyJson){
	$.ajax({
       type:"POST",
       contentType:'application/json',
       url:ip + "/api/add-label-property-task/",
       dataType:"json",
       async:false,
       headers: {
          authorization:token,
        },
       data:JSON.stringify({'task_name':task_name,
                            'task_type':task_type,
                            'propertyJson':propertyJson,
							"task_desc":""
       }),
       success:function(res){
		  console.log(res);
		  if(res.code == 0){
			 createsucced = true;
		  }
		  else{
			 createsucced = false;
		  }
		},
	    error:function(response) {
		  redirect(response);
		  createsucced = false;
        }
   });
	
}



function add_new_attribute_from_user_input() {
  var attr_id = document.getElementById('user_input_attribute_id').value;
  if ( attr_id === '' ) {
    show_message('Enter the name of attribute that you wish to add');
    return;
  }

  if ( attribute_property_id_exists(attr_id) ) {
    show_message('The region attribute [' + attr_id + '] already exists.');
  } else {
    _via_current_attribute_id = attr_id;
    add_new_attribute(attr_id);
    update_attributes_update_panel();
    annotation_editor_update_content(regions);
    document.getElementById('attribute_properties').innerHTML = '';
    document.getElementById('attribute_options').innerHTML = '';

	$('#user_input_attribute_id').val("");
    show_message('Added ' + _via_attribute_being_updated + ' attribute [' + attr_id + '].');
  }
}

function show_message(msg){
	console.log(msg);
}

function show_attribute_properties() {

  document.getElementById('attribute_properties').innerHTML = '';

  var attr_id = sessionStorage.getItem("attr_id");
  var attr_type = _via_attribute_being_updated;
  

  var attr_input_type = _via_attributes[attr_type][attr_id].type;

  var attr_desc = _via_attributes[attr_type][attr_id].description;

  attribute_property_add_input_property('Name of attribute (appears in exported annotations)',
                                        'Name',
                                        attr_id,
                                        'attribute_name');
  attribute_property_add_input_property('Description of attribute (shown to user during annotation session)',
                                        'Desc.',
                                        attr_desc,
                                        'attribute_description');

  if ( attr_input_type === 'text' ) {
    var attr_default_value = _via_attributes[attr_type][attr_id].default_value;
    attribute_property_add_input_property('Default value of this attribute',
                                          'Def.',
                                          attr_default_value,
                                          'attribute_default_value');
  }

  // add dropdown for type of attribute
  var p = document.createElement('div');
  p.setAttribute('class', 'property');
  var c0 = document.createElement('span');
  c0.setAttribute('title', 'Attribute type (e.g. text, checkbox, radio, etc)');
  c0.innerHTML = 'Type';
  var c1 = document.createElement('span');
  var c1b = document.createElement('select');
  c1b.setAttribute('onchange', 'attribute_property_on_update(this)');
  c1b.setAttribute('id', 'attribute_type');
  var type_id;
  for ( type_id in VIA_ATTRIBUTE_TYPE ) {
    var type = VIA_ATTRIBUTE_TYPE[type_id];
    var option = document.createElement('option');
    option.setAttribute('value', type);
    option.innerHTML = type;
    if ( attr_input_type == type ) {
      option.setAttribute('selected', 'selected');
    }
    c1b.appendChild(option);
  }
  c1.appendChild(c1b);
  p.appendChild(c0);
  p.appendChild(c1);
  document.getElementById('attribute_properties').appendChild(p);
}

function show_attribute_options() {
  // var attr_list = document.getElementById('attributes_name_list');
  document.getElementById('attribute_options').innerHTML = '';
   document.getElementById('attribute_options').innerHTML = '';


  // var attr_id = attr_list.value;
  var attr_id = sessionStorage.getItem("attr_id");
  var attr_type = _via_attributes[_via_attribute_being_updated][attr_id].type;

  // populate additional options based on attribute type
  switch( attr_type ) {
  case VIA_ATTRIBUTE_TYPE.TEXT:
    // text does not have any additional properties
    break;
  case VIA_ATTRIBUTE_TYPE.CHECKBOX: // handled by next case
  case VIA_ATTRIBUTE_TYPE.DROPDOWN: // handled by next case
  case VIA_ATTRIBUTE_TYPE.RADIO:
    var p = document.createElement('div');
    p.setAttribute('class', 'property');
    p.setAttribute('style', 'text-align:center');
    var c0 = document.createElement('span');
    c0.setAttribute('style', 'width:25%');
    c0.setAttribute('title', 'When selected, this is the value that appears in exported annotations');
    c0.innerHTML = 'id';
    var c1 = document.createElement('span');
    c1.setAttribute('style', 'width:60%');
    c1.setAttribute('title', 'This is the text shown as an option to the annotator');
    c1.innerHTML = 'description';
    var c2 = document.createElement('span');
    c2.setAttribute('title', 'The default value of this attribute');
    c2.innerHTML = 'def.';
    p.appendChild(c0);
    p.appendChild(c1);
    p.appendChild(c2);
    document.getElementById('attribute_options').appendChild(p);

    var options = _via_attributes[_via_attribute_being_updated][attr_id].options;
    var option_id;
    for ( option_id in options ) {
      var option_desc = options[option_id];

      var option_default = _via_attributes[_via_attribute_being_updated][attr_id].default_options[option_id];
      attribute_property_add_option(attr_id, option_id, option_desc, option_default, attr_type);
    }
    attribute_property_add_new_entry_option(attr_id, attr_type);
    break;
  default:
    console.log('Attribute type ' + attr_type + ' is unavailable');
  }
}

function attribute_property_add_input_property(title, name, value, id) {
  var p = document.createElement('div');
  p.setAttribute('class', 'property');
  var c0 = document.createElement('span');
  c0.setAttribute('title', title);
  c0.innerHTML = name;
  var c1 = document.createElement('span');
  var c1b = document.createElement('input');
  c1b.setAttribute('onchange', 'attribute_property_on_update(this)');
  if ( typeof(value) !== 'undefined' ) {
    c1b.setAttribute('value', value);
  }
  c1b.setAttribute('id', id);
  c1.appendChild(c1b);
  p.appendChild(c0);
  p.appendChild(c1);

  document.getElementById('attribute_properties').appendChild(p);
}

function attribute_property_add_option(attr_id, option_id, option_desc, option_default, attribute_type) {
  var p = document.createElement('div');
  p.setAttribute('class', 'property');
  var c0 = document.createElement('span');
  var c0b = document.createElement('input');
  c0b.setAttribute('type', 'text');
  c0b.setAttribute('value', option_id);
  c0b.setAttribute('title', option_id);
  c0b.setAttribute('onchange', 'attribute_property_on_option_update(this)');
  c0b.setAttribute('id', '_via_attribute_option_id_' + option_id);

  var c1 = document.createElement('span');
  var c1b = document.createElement('input');
  c1b.setAttribute('type', 'text');

  if ( attribute_type === VIA_ATTRIBUTE_TYPE.IMAGE ) {
    var option_desc_info = option_desc.length + ' bytes of base64 image data';
    c1b.setAttribute('value', option_desc_info);
    c1b.setAttribute('title', 'To update, copy and paste base64 image data in this text box');
  } else {
    c1b.setAttribute('value', option_desc);
    c1b.setAttribute('title', option_desc);
  }
  c1b.setAttribute('onchange', 'attribute_property_on_option_update(this)');
  c1b.setAttribute('id', '_via_attribute_option_description_' + option_id);

  var c2 = document.createElement('span');
  var c2b = document.createElement('input');
  c2b.setAttribute('type', attribute_type);
  if ( typeof option_default !== 'undefined' ) {
    c2b.checked = option_default;
  }
  if ( attribute_type === 'radio' || attribute_type === 'image' || attribute_type === 'dropdown' ) {
    // ensured that user can activate only one radio button
    c2b.setAttribute('type', 'radio');
    c2b.setAttribute('name', attr_id);
  }

  c2b.setAttribute('onchange', 'attribute_property_on_option_update(this)');
  c2b.setAttribute('id', '_via_attribute_option_default_' + option_id);

  c0.appendChild(c0b);
  c1.appendChild(c1b);
  c2.appendChild(c2b);
  p.appendChild(c0);
  p.appendChild(c1);
  p.appendChild(c2);

  document.getElementById('attribute_options').appendChild(p);
}

function attribute_property_on_option_update(p) {
  var attr_id = get_current_attribute_id();
  if ( p.id.startsWith('_via_attribute_option_id_') ) {
    var old_key = p.id.substr( '_via_attribute_option_id_'.length );
    var new_key = p.value;
    if ( old_key !== new_key ) {
      var option_id_test = attribute_property_option_id_is_valid(attr_id, new_key);
      if ( option_id_test.is_valid ) {
		  if ( new_key === '' || typeof(new_key) === 'undefined' ) {
			// an empty new_option_id indicates deletion of option_id
			delete _via_attributes[_via_attribute_being_updated][attr_id]['options'][old_key];
			delete _via_attributes[_via_attribute_being_updated][attr_id]['default_options'][old_key];
			show_attribute_options();
		  } 
		  else {
			var des = _via_attributes[_via_attribute_being_updated][attr_id]['options'][old_key];
			var default_value = _via_attributes[_via_attribute_being_updated][attr_id]['default_options'][old_key];
			delete _via_attributes[_via_attribute_being_updated][attr_id]['options'][old_key];
			delete _via_attributes[_via_attribute_being_updated][attr_id]['default_options'][old_key];
			
			_via_attributes[_via_attribute_being_updated][attr_id]['options'][new_key]=des;
			_via_attributes[_via_attribute_being_updated][attr_id]['default_options'][new_key] = default_value;
			
		  }

        //todo 变更
      } else {
        p.value = old_key; // restore old value
        show_message( option_id_test.message );
        show_attribute_properties();
      }
      return;
    }
  }

  if ( p.id.startsWith('_via_attribute_option_description_') ) {
    var key = p.id.substr( '_via_attribute_option_description_'.length );
    var old_value = _via_attributes[_via_attribute_being_updated][attr_id].options[key];
    var new_value = p.value;
    if ( new_value !== old_value ) {
      _via_attributes[_via_attribute_being_updated][attr_id].options[key] = new_value;
      show_attribute_properties();
      annotation_editor_update_content(regions);
    }
  }

  if ( p.id.startsWith('_via_attribute_option_default_') ) {
    var new_default_option_id = p.id.substr( '_via_attribute_option_default_'.length );
    var old_default_option_id_list = Object.keys(_via_attributes[_via_attribute_being_updated][attr_id].default_options);

    if ( old_default_option_id_list.length === 0 ) {
      // default set for the first time
      _via_attributes[_via_attribute_being_updated][attr_id].default_options[new_default_option_id] = p.checked;
    } else {
      switch ( _via_attributes[_via_attribute_being_updated][attr_id].type ) {
      case 'image':    // fallback
      case 'dropdown': // fallback
      case 'radio':    // fallback
        // to ensure that only one radio button is selected at a time
        _via_attributes[_via_attribute_being_updated][attr_id].default_options = {};
        _via_attributes[_via_attribute_being_updated][attr_id].default_options[new_default_option_id] = p.checked;
        break;
      case 'checkbox':
        _via_attributes[_via_attribute_being_updated][attr_id].default_options[new_default_option_id] = p.checked;
        break;
      }
    }
    // default option updated
	show_attribute_properties();
    annotation_editor_update_content(regions);
												  
  }
}



function attribute_property_option_id_is_valid(attr_id, new_option_id) {
  var option_id;
  for ( option_id in _via_attributes[_via_attribute_being_updated][attr_id].options ) {
    if ( option_id === new_option_id ) {
      return { 'is_valid':false, 'message':'Option id [' + attr_id + '] already exists' };
    }
  }

  if ( new_option_id.includes('__') ) { // reserved separator for attribute-id, row-id, option-id
    return {'is_valid':false, 'message':'Option id cannot contain two consecutive underscores'};
  }

  return {'is_valid':true};
}
function attribute_property_add_new_entry_option(attr_id, attribute_type) {
  var p = document.createElement('div');
  p.setAttribute('class', 'new_option_id_entry');
  var c0b = document.createElement('input');
  c0b.setAttribute('type', 'text');
  c0b.setAttribute('onchange', 'attribute_property_on_option_add(this)');
  c0b.setAttribute('id', '_via_attribute_new_option_id');
  c0b.setAttribute('placeholder', 'Add new option id');
  p.appendChild(c0b);
  document.getElementById('attribute_options').appendChild(p);
}

function get_current_attribute_id() {
   return sessionStorage.getItem("attr_id");
}

function attribute_property_on_update(p) {
  var attr_id = get_current_attribute_id();
  var attr_type = _via_attribute_being_updated;
  var attr_value = p.value;

  switch(p.id) {
  case 'attribute_name':
    if ( attr_value !== attr_id ) {
      Object.defineProperty(_via_attributes[attr_type],
                            attr_value,
                            Object.getOwnPropertyDescriptor(_via_attributes[attr_type], attr_id));

      delete _via_attributes[attr_type][attr_id];
      update_attributes_update_panel();
      annotation_editor_update_content(regions);
    }
    break;
  case 'attribute_description':
    _via_attributes[attr_type][attr_id].description = attr_value;
    update_attributes_update_panel();
    annotation_editor_update_content(regions);
    break;
  case 'attribute_default_value':
    _via_attributes[attr_type][attr_id].default_value = attr_value;
    update_attributes_update_panel();
    annotation_editor_update_content(regions);
    break;
  case 'attribute_type':
    _via_attributes[attr_type][attr_id].type = attr_value;
    if( attr_value === VIA_ATTRIBUTE_TYPE.TEXT ) {
      _via_attributes[attr_type][attr_id].default_value = '';
      delete _via_attributes[attr_type][attr_id].options;
      delete _via_attributes[attr_type][attr_id].default_options;
    } else {
      // preserve existing options
      if ( ! _via_attributes[attr_type][attr_id].hasOwnProperty('options') ) {
        _via_attributes[attr_type][attr_id].options = {};
        _via_attributes[attr_type][attr_id].default_options = {};
      }

      if ( _via_attributes[attr_type][attr_id].hasOwnProperty('default_value') ) {
        delete _via_attributes[attr_type][attr_id].default_value;
      }

      // collect existing attribute values and add them as options
      var attr_values = attribute_get_unique_values(attr_type, attr_id);
      var i;
      for ( i = 0; i < attr_values.length; ++i ) {
        var attr_val = attr_values[i];
        if ( attr_val !== '' ) {
          _via_attributes[attr_type][attr_id].options[attr_val] = attr_val;
        }
      }
    }
    show_attribute_properties();
    show_attribute_options();
    annotation_editor_update_content(regions);
    break;
  }
}

function attribute_get_unique_values(attr_type, attr_id) {
  var values = [];
  switch ( attr_type ) {

  case 'region':
    var img_id, attr_val, i;
    // for ( img_id in _via_img_metadata ) {
     if (regions!== undefined){
      for ( i = 0; i < regions.length; ++i ) {
        // if (_via_img_metadata[img_id].regions[i].hasOwnProperty("region_attributes")){
        if (regions[i]!== undefined){
            if ( regions[i].other.region_attributes.hasOwnProperty(attr_id) ) {
              attr_val = regions[i].other.region_attributes[attr_id];
              if ( ! values.includes(attr_val) ) {
                values.push(attr_val);
              }
            }
        }


      }
        }
    // }
    break;
  default:
    break;
  }
  return values;
}

function attribute_property_on_option_add(p) {
  if ( p.value === '' || p.value === null ) {
    return;
  }

  if ( p.id === '_via_attribute_new_option_id' ) {
    var attr_id = get_current_attribute_id();
    var option_id = p.value;
    var option_id_test = attribute_property_option_id_is_valid(attr_id, option_id);
    if ( option_id_test.is_valid ) {
      _via_attributes[_via_attribute_being_updated][attr_id].options[option_id] = '';
      show_attribute_options();
      annotation_editor_update_content(regions);
    } else {
      show_message( option_id_test.message );
      attribute_property_reset_new_entry_inputs();
    }
  }
}

function attribute_property_reset_new_entry_inputs() {
  var container = document.getElementById('attribute_options');
  var p = container.lastChild;
  console.log(p.childNodes)
  if ( p.childNodes[0] ) {
    p.childNodes[0].value = '';
  }
  if ( p.childNodes[1] ) {
    p.childNodes[1].value = '';
  }
}

function attribute_property_option_id_is_valid(attr_id, new_option_id) {
  var option_id;
  for ( option_id in _via_attributes[_via_attribute_being_updated][attr_id].options ) {
    if ( option_id === new_option_id ) {
      return { 'is_valid':false, 'message':'Option id [' + attr_id + '] already exists' };
    }
  }

  if ( new_option_id.includes('__') ) { // reserved separator for attribute-id, row-id, option-id
    return {'is_valid':false, 'message':'Option id cannot contain two consecutive underscores'};
  }

  return {'is_valid':true};
}

function annotation_editor_update_content(recttype) {
  console.log("_via_attributes =" + JSON.stringify(_via_attributes))
  /*
  return new Promise( function(ok_callback, err_callback) {
    var ae = document.getElementById('annotation_editor');
    if (ae ) {

      annotation_editor_update_metadata_html(recttype);
    }
    ok_callback();
  });
  */
}


function annotation_editor_update_metadata_html(recttype) {

  //var ae = document.getElementById('annotation_editor');
  switch ( _via_metadata_being_updated ) {
  case 'region':
    var rindex;
    // ae.appendChild( annotation_editor_get_metadata_row_html(_via_user_sel_region_id) )
    if ( _via_display_area_content_name === VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE_GRID ) {
      //ae.appendChild( annotation_editor_get_metadata_row_html(0) );
	  annotation_editor_get_metadata_row_html(0)
    } else {
      if ( _via_display_area_content_name === VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE ) {
        if ( _via_annotation_editor_mode === VIA_ANNOTATION_EDITOR_MODE.SINGLE_REGION ) {
		 
			//ae.appendChild( annotation_editor_get_metadata_row_html(_via_user_sel_region_id,recttype) );
			
			annotation_editor_get_metadata_row_html(_via_user_sel_region_id,recttype);
			  
		  
        } else {
          for ( rindex = 0; rindex < regions.length; ++rindex ) {
            //ae.appendChild( annotation_editor_get_metadata_row_html(rindex) );
			
			annotation_editor_get_metadata_row_html(rindex)
          }
        }
      }
    }
    break;

  case 'file':
    ae.appendChild( annotation_editor_get_metadata_row_html(0) );
    break;
  }
}

function annotation_editor_get_metadata_row_html(row_id,recttype) {
  var row = document.createElement('div');
  row.setAttribute('class', 'row');
  row.setAttribute('id', 'ae_' + _via_metadata_being_updated + '_' + row_id);
  console.log("row_id=" + row_id);
  if ( _via_metadata_being_updated === 'region' ) {
    var rid = document.createElement('span');

    switch(_via_display_area_content_name) {
    case VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE_GRID:
      rid.setAttribute('class', 'col');
      rid.innerHTML = 'Grouped regions in ' + _via_image_grid_selected_img_index_list.length + ' files';
      break;
    case VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE:
      rid.setAttribute('class', 'col id');
      rid.innerHTML = (row_id );
      break;
    }
    row.appendChild(rid);
  }

 

  var attr_id;
  for ( attr_id in _via_attributes[_via_metadata_being_updated] ) {

	//console.log("attr_id row=" + attr_id);
	if(attr_id =="verify" && userType !=2 ){//标注人员不需要看到审核列
		continue;
	}
    var col = document.createElement('span');
    col.setAttribute('class', 'col');

    var attr_type    = _via_attributes[_via_metadata_being_updated][attr_id].type;
    var attr_desc    = _via_attributes[_via_metadata_being_updated][attr_id].desc;
    if ( typeof(attr_desc) === 'undefined' ) {
      attr_desc = '';
    }
    var attr_html_id = attr_id + '__' + row_id;

    var attr_value = '';
    var attr_placeholder = '';
    if ( _via_display_area_content_name === VIA_DISPLAY_AREA_CONTENT_NAME.IMAGE ) {
	  console.log("tt=" + _via_display_area_content_name);
      switch(_via_metadata_being_updated) {
      case 'region':
      if (regions[row_id]!==undefined){
        if ( regions[row_id].other.region_attributes.hasOwnProperty(attr_id) ) {
            attr_value = regions[row_id].other.region_attributes[attr_id];
            if (isEmpty(attr_value) && attr_id == "id"){
              attr_value = maxIdNum + 1;
              regions[row_id].other.region_attributes[attr_id]= attr_value;
              maxIdNum = maxIdNum + 1;

            }
        } else {
          attr_placeholder = 'not defined yet!';
        }

      }

      case 'file':
         attr_placeholder = 'not defined yet!';
        // if ( _via_img_metadata[_via_image_id].file_attributes.hasOwnProperty(attr_id) ) {
        //   attr_value = _via_img_metadata[_via_image_id].file_attributes[attr_id];
        // } else {
        //   attr_placeholder = 'not defined yet!';
        // }
      }
    }


    switch(attr_type) {
    case 'text':
      col.innerHTML = '<textarea ' +
        'onchange="annotation_editor_on_metadata_update(this)" ' +
        'onfocus="annotation_editor_on_metadata_focus(this)" ' +
        'title="' + attr_desc + '" ' +
        'placeholder="' + attr_placeholder + '" ' +
        'id="' + attr_html_id + '">' + attr_value + '</textarea>';
      break;
    case 'checkbox':
      var options = _via_attributes[_via_metadata_being_updated][attr_id].options;
      var option_id;
      for ( option_id in options ) {
        var option_html_id = attr_html_id + '__' + option_id;
        var option = document.createElement('input');
        option.setAttribute('type', 'checkbox');
        option.setAttribute('value', option_id);
        option.setAttribute('id', option_html_id);
        option.setAttribute('onfocus', 'annotation_editor_on_metadata_focus(this)');
        option.setAttribute('onchange', 'annotation_editor_on_metadata_update(this)');

        var option_desc  = _via_attributes[_via_metadata_being_updated][attr_id].options[option_id];
        if ( option_desc === '' || typeof(option_desc) === 'undefined' ) {
          // option description is optional, use option_id when description is not present
          option_desc = option_id;
        }

        // set the value of options based on the user annotations
        if ( typeof attr_value !== 'undefined') {
          if ( attr_value.hasOwnProperty(option_id) ) {
            option.checked = attr_value[option_id];
          }
        }

        var label  = document.createElement('label');
        label.setAttribute('for', option_html_id);
        label.innerHTML = option_desc;

        var container = document.createElement('span');
        container.appendChild(option);
        container.appendChild(label);
        col.appendChild(container);
      }
      break;
    case 'radio':
      var option_id;
      for ( option_id in _via_attributes[_via_metadata_being_updated][attr_id].options ) {
        var option_html_id = attr_html_id + '__' + option_id;
        var option = document.createElement('input');
        option.setAttribute('type', 'radio');
        option.setAttribute('name', attr_html_id);
        option.setAttribute('value', option_id);
        option.setAttribute('id', option_html_id);
        option.setAttribute('onfocus', 'annotation_editor_on_metadata_focus(this)');
        option.setAttribute('onchange', 'annotation_editor_on_metadata_update(this)');

        var option_desc  = _via_attributes[_via_metadata_being_updated][attr_id].options[option_id];
        if ( option_desc === '' || typeof(option_desc) === 'undefined' ) {
          // option description is optional, use option_id when description is not present
          option_desc = option_id;
        }

        if ( attr_value === option_id ) {
          option.checked = true;
        }

        var label  = document.createElement('label');
        label.setAttribute('for', option_html_id);
        label.innerHTML = option_desc;

        var container = document.createElement('span');
        container.appendChild(option);
        container.appendChild(label);
        col.appendChild(container);
      }
      break;
    case 'image':
      var option_id;
      var option_count = 0;
      for ( option_id in _via_attributes[_via_metadata_being_updated][attr_id].options ) {
        option_count = option_count + 1;
      }
      var img_options = document.createElement('div');
      img_options.setAttribute('class', 'img_options');
      col.appendChild(img_options);

      var option_index = 0;
      for ( option_id in _via_attributes[_via_metadata_being_updated][attr_id].options ) {
        var option_html_id = attr_html_id + '__' + option_id;
        var option = document.createElement('input');
        option.setAttribute('type', 'radio');
        option.setAttribute('name', attr_html_id);
        option.setAttribute('value', option_id);
        option.setAttribute('id', option_html_id);
        option.setAttribute('onfocus', 'annotation_editor_on_metadata_focus(this)');
        option.setAttribute('onchange', 'annotation_editor_on_metadata_update(this)');

        var option_desc  = _via_attributes[_via_metadata_being_updated][attr_id].options[option_id];
        if ( option_desc === '' || typeof(option_desc) === 'undefined' ) {
          // option description is optional, use option_id when description is not present
          option_desc = option_id;
        }

        if ( attr_value === option_id ) {
          option.checked = true;
        }

        var label  = document.createElement('label');
        label.setAttribute('for', option_html_id);
        label.innerHTML = '<img src="' + option_desc + '"><p>' + option_id + '</p>';

        var container = document.createElement('span');
        container.appendChild(option);
        container.appendChild(label);
        img_options.appendChild(container);
      }
      break;

    case 'dropdown':
      var sel = document.createElement('select');
      sel.setAttribute('id', attr_html_id);
      sel.setAttribute('onfocus', 'annotation_editor_on_metadata_focus(this)');
      sel.setAttribute('onchange', 'annotation_editor_on_metadata_update(this)');
      var option_id;
      var option_selected = false;
      for ( option_id in _via_attributes[_via_metadata_being_updated][attr_id].options ) {
        var option_html_id = attr_html_id + '__' + option_id;
        var option = document.createElement('option');
        option.setAttribute('value', option_id);

        var option_desc  = _via_attributes[_via_metadata_being_updated][attr_id].options[option_id];
        if ( option_desc === '' || typeof(option_desc) === 'undefined' ) {
          // option description is optional, use option_id when description is not present
          option_desc = option_id;
        }

        if ( option_id === attr_value ) {
          option.setAttribute('selected', 'selected');
          option_selected = true;
        }
        option.innerHTML = option_desc;
        sel.appendChild(option);
      }

      if ( ! option_selected ) {
        sel.selectedIndex = '-1';
      }
      col.appendChild(sel);
      break;

    }

    row.appendChild(col);
  }

  return row;
}


function update_attributes_update_panel() {
    update_attributes_name_list();
}

function update_attributes_name_list() {
  var p = document.getElementById('attributes_name_list');
  p.innerHTML = '';

  var attr;

  var html=" <tr>\
            <th>属性值</th>\
            <th>操作</th>\
            </tr> ";

  for ( attr in _via_attributes[_via_attribute_being_updated] ){
	  var row = "";
	  if( attr == "id"){
		  row = "<tr>\
                  <td id =\"attr\">"+attr+ "</td>"+
                  "<td>"+
                  "<a onclick=\"sessionStorage.setItem(\'attr_id\',\'"+attr+"\'); show_attribute_properties(); show_attribute_options()\" class=\"btn btn-xs btn-success\">" +"显示属性"+ "</a>"  + "&nbsp;&nbsp;&nbsp;" +
                  
                  "</td>"+
                "</tr>";
	  }else{
		  row = "<tr>\
                  <td id =\"attr\">"+attr+ "</td>"+
                  "<td>"+
                  "<a onclick=\"sessionStorage.setItem(\'attr_id\',\'"+attr+"\'); show_attribute_properties(); show_attribute_options()\" class=\"btn btn-xs btn-success\">" +"显示属性"+ "</a>"  + "&nbsp;&nbsp;&nbsp;<a onclick=\"sessionStorage.setItem(\'attr_id\',\'"+attr+"\');delete_existing_attribute_with_confirm()\"; class=\"btn btn-xs btn-success\">删除属性</a>" +
                  
                  "</td>"+
                "</tr>";
		  
	  }
	  
  

      html=html+row;
  }
  document.getElementById('attributes_name_list').innerHTML=html;
  //console.log('here to this.222  html=' + html);

}

function delete_existing_attribute_with_confirm() {
  
   var attr_id = sessionStorage.getItem("attr_id");
  if ( attr_id === '' ) {
    show_message('Enter the name of attribute that you wish to delete');
    return;
  }
  if ( attribute_property_id_exists(attr_id) ) {
    var config = {'title':'Delete ' + _via_attribute_being_updated + ' attribute [' + attr_id + ']' };
    var input = { 'attr_type':{'type':'text', 'name':'Attribute Type', 'value':_via_attribute_being_updated, 'disabled':true},
                  'attr_id':{'type':'text', 'name':'Attribute Id', 'value':attr_id, 'disabled':true}
                };

    delete_existing_attribute_confirmed(input);

  } else {
    show_message('Attribute [' + attr_id + '] does not exist!');
    return;
  }
}

function delete_existing_attribute_confirmed(input) {
  var attr_type = input.attr_type.value;
  var attr_id   = input.attr_id.value;
  delete_existing_attribute(attr_type, attr_id);
  document.getElementById('user_input_attribute_id').value = '';
  show_message('Deleted ' + attr_type + ' attribute [' + attr_id + ']');
 
  document.getElementById('attribute_properties').innerHTML = '';
  document.getElementById('attribute_options').innerHTML = '';
}

function delete_existing_attribute(attribute_type, attribute_id) {
  if ( _via_attributes[attribute_type].hasOwnProperty( attribute_id ) ) {
    var attr_id_list = Object.keys(_via_attributes[attribute_type]);
    if ( attr_id_list.length === 1 ) {
      _via_current_attribute_id = '';
    } else {
      var current_index = attr_id_list.indexOf(attribute_id);
      var next_index = current_index + 1;
      if ( next_index === attr_id_list.length ) {
        next_index = current_index - 1;
      }
      _via_current_attribute_id = attr_id_list[next_index];
    }
    delete _via_attributes[attribute_type][attribute_id];
    update_attributes_update_panel();
    // annotation_editor_update_content();
  }
}

function add_new_attribute(attribute_id) {
  _via_attributes[_via_attribute_being_updated][attribute_id] = {};
  _via_attributes[_via_attribute_being_updated][attribute_id].type = 'text';
  _via_attributes[_via_attribute_being_updated][attribute_id].description = '';
  _via_attributes[_via_attribute_being_updated][attribute_id].default_value = '';
}



function attribute_property_id_exists(name) {
  var attr_name;
  for ( attr_name in _via_attributes[_via_attribute_being_updated] ) {
    if ( attr_name === name ) {
      return true;
    }
  }
  return false;
}
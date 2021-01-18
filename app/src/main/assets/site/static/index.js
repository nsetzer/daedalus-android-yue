api={"requests":{}};
daedalus=(function(){
    "use strict";
    const env={"baseUrl":"https://yueapp.duckdns.org"};
    const build_platform="android";
    const[StyleSheet,getStyleSheet,parseParameters,util]=(function(){
        function array_move(arr,p1,p2){
          if(p1<0){
            p1=0;
          }
          if(p2<0){
            p2=0;
          }
          if(p1>arr.length){
            p1=arr.length;
          }
          if(p2>arr.length){
            p2=arr.length;
          }
          if(p1==p2){
            return;
          }
          arr.splice(p2,0,arr.splice(p1,1)[0]);
          return;
        }
        function randomFloat(min,max){
          return Math.random()*(max-min)+min;
        }
        function randomInt(min,max){
          min=Math.ceil(min);
          max=Math.floor(max);
          return Math.floor(Math.random()*(max-min+1))+min;
        }
        function object2style_helper(prefix,obj){
          const items=Object.keys(obj).map(key=>{
              const type=typeof(obj[key]);
              if(type==="object"){
                return object2style_helper(prefix+key+"-",obj[key]);
              }else{
                return[prefix+key+": "+obj[key]];
              }
            });
          return[].concat.apply([],items);
        }
        function object2style(obj){
          const arr=object2style_helper("",obj);
          return[].concat.apply([],arr).join(';');
        }
        function serializeParameters(obj){
          if(Object.keys(obj).length==0){
            return"";
          }
          const strings=Object.keys(obj).reduce(function(a,k){
              if(obj[k]===null||obj[k]===undefined){

              }else if(Array.isArray(obj[k])){
                for(let i=0;i<obj[k].length;i++)
                {
                  a.push(encodeURIComponent(k)+'='+encodeURIComponent(obj[k][i]));
                  
                }
              }else{
                a.push(encodeURIComponent(k)+'='+encodeURIComponent(obj[k]));
              }
              return a;
            },[]);
          return'?'+strings.join('&');
        }
        function parseParameters(text=undefined){
          let match,search=/([^&=]+)=?([^&]*)/g,decode=s=>decodeURIComponent(s.replace(
                          /\+/g," ")),query=(text===undefined)?window.location.search.substring(
                      1):text;
          let urlParams={};
          while(match=search.exec(query)){
            let value=decode(match[2]);
            let key=decode(match[1]);
            if(urlParams[key]===undefined){
              urlParams[key]=[value];
            }else{
              urlParams[key].push(value);
            }
          }
          return urlParams;
        }
        function isFunction(x){
          return(x instanceof Function);
        }
        function joinpath(...parts){
          let str="";
          for(let i=0;i<parts.length;i++)
          {
            if(!str.endsWith("/")&&!parts[i].startsWith("/")){
              str+="/";
            }
            str+=parts[i];
          }
          return str;
        }
        function splitpath(path){
          const parts=path.split('/');
          if(parts.length>0&&parts[parts.length-1].length===0){
            parts.pop();
          }
          return parts;
        }
        function dirname(path){
          const parts=path.split('/');
          while(parts.length>0&&parts[parts.length-1].length===0){
            parts.pop();
          }
          return joinpath(...parts.slice(0,-1));
        }
        function splitext(name){
          const index=name.lastIndexOf('.');
          if(index<=0||name[index-1]=='/'){
            return[name,''];
          }else{
            return[name.slice(0,index),name.slice(index)];
          }
        }
        let css_sheet=null;
        let selector_names={};
        function generateStyleSheetName(){
          const chars='abcdefghijklmnopqrstuvwxyz';
          let name;
          do {
            name="css-";
            for(let i=0;i<6;i++)
            {
              let c=chars[randomInt(0,chars.length-1)];
              name+=c;
            }
          } while (selector_names[name]!==undefined)
          return name;
        }
        function shuffle(array){
          let currentIndex=array.length,temporaryValue,randomIndex;
          while(0!==currentIndex){
            randomIndex=Math.floor(Math.random()*currentIndex);
            currentIndex-=1;
            temporaryValue=array[currentIndex];
            array[currentIndex]=array[randomIndex];
            array[randomIndex]=temporaryValue;
          }
          return array;
        }
        function StyleSheet(...args){
          let name;
          let style;
          let selector;
          if(args.length===1){
            name=generateStyleSheetName();
            selector="."+name;
            style=args[0];
          }else if(args.length===2){
            selector=args[0];
            style=args[1];
            name=selector;
          }
          if(css_sheet===null){
            css_sheet=document.createElement('style');
            css_sheet.type='text/css';
            document.head.appendChild(css_sheet);
          }
          const text=object2style(style);
          selector_names[name]=style;
          if(!(css_sheet.sheet||{}).insertRule){
            (css_sheet.styleSheet||css_sheet.sheet).addRule(selector,text);
          }else{
            css_sheet.sheet.insertRule(selector+"{"+text+"}",css_sheet.sheet.rules.length);
            
          }
          return name;
        }
        function getStyleSheet(name){
          return selector_names[name];
        }
        function perf_timer(){
          return performance.now();
        }
        const util={array_move,randomInt,randomFloat,object2style,serializeParameters,
                  parseParameters,isFunction,joinpath,splitpath,dirname,splitext,shuffle,
                  perf_timer};
        return[StyleSheet,getStyleSheet,parseParameters,util];
      })();
    const[ButtonElement,DomElement,DraggableList,DraggableListItem,HeaderElement,
          LinkElement,ListElement,ListItemElement,NumberInputElement,Signal,TextElement,
          TextInputElement]=(function(){
        let sigal_counter=0;
        function Signal(element,name){
          const event_name="onSignal_"+(sigal_counter++)+"_"+name;
          const signal={};
          signal._event_name=event_name;
          signal._slots=[];
          signal.emit=(obj=null)=>{
            signal._slots.map(item=>{
                requestIdleCallback(()=>{
                    item.callback(obj);
                  });
              });
          };
          console.log("signal create:"+event_name);
          if(!!element){
            element.signals.push(signal);
          }
          return signal;
        }
        let element_uid=0;
        function generateElementId(){
          const chars='abcdefghijklmnopqrstuvwxyz';
          let name;
          name="-";
          for(let i=0;i<6;i++)
          {
            let c=chars[util.randomInt(0,chars.length-1)];
            name+=c;
          }
          return name+"-"+(element_uid++);
        }
        class DomElement{
          constructor(type="div",props=undefined,children=undefined){
            if(type===undefined){
              throw`DomElement type is undefined. super called with ${arguments.length} arguments`;
              
            }
            this.type=type;
            if(props===undefined){
              this.props={};
            }else{
              this.props=props;
            }
            if(this.props.id===undefined){
              this.props.id=this.constructor.name+generateElementId();
            }
            if(children===undefined){
              this.children=[];
            }else{
              this.children=children;
            }
            this.signals=[];
            this.slots=[];
            this.dirty=true;
            this.state={};
            this.attrs={};
            this._fiber=null;
            Object.getOwnPropertyNames(this.__proto__).filter(key=>key.startsWith(
                              "on")).forEach(key=>{
                this.props[key]=this[key].bind(this);
              });
          }
          _update(element){

          }
          update(){
            this._update(this);
          }
          updateState(state,doUpdate){
            const newState={...this.state,...state};
            if(doUpdate!==false){
              if((doUpdate===true)||(this.elementUpdateState===undefined)||(this.elementUpdateState(
                                      this.state,newState)!==false)){
                this.update();
              }
            }
            this.state=newState;
          }
          updateProps(props,doUpdate){
            const newProps={...this.props,...props};
            if(doUpdate!==false){
              if((doUpdate===true)||(this.elementUpdateProps===undefined)||(this.elementUpdateProps(
                                      this.props,newProps)!==false)){
                this.update();
              }
            }
            this.props=newProps;
          }
          appendChild(childElement){
            if(!childElement||!childElement.type){
              throw"invalid child";
            }
            if(typeof this.children==="string"){
              this.children=[this.children];
            }else if(typeof this.children==="undefined"){
              this.children=[];
            }
            this.children.push(childElement);
            this.update();
            return childElement;
          }
          insertChild(index,childElement){
            if(!childElement||!childElement.type){
              throw"invalid child";
            }
            if(index<0){
              index+=this.children.length+1;
            }
            if(index<0||index>this.children.length){
              console.error("invalid index: "+index);
              return;
            }
            if(typeof this.children==="string"){
              this.children=[this.children];
            }else if(typeof this.children==="undefined"){
              this.children=[];
            }
            this.children.splice(index,0,childElement);
            this.update();
            return childElement;
          }
          removeChild(childElement){
            if(!childElement||!childElement.type){
              throw"invalid child";
            }
            const index=this.children.indexOf(childElement);
            if(index>=0){
              this.children.splice(index,1);
              this.update();
            }else{
              console.error("child not in list");
            }
          }
          removeChildren(){
            this.children.splice(0,this.children.length);
            this.update();
          }
          replaceChild(childElement,newChildElement){
            const index=this.children.indexOf(childElement);
            if(index>=0){
              this.children[index]=newChildElement;
              this.update();
            }
          }
          addClassName(cls){
            let props;
            if(!this.props.className){
              props={className:cls};
            }else if(Array.isArray(this.props.className)){
              props={className:[cls,...this.props.className]};
            }else{
              props={className:[cls,this.props.className]};
            }
            this.updateProps(props);
          }
          removeClassName(cls){
            let props;
            if(Array.isArray(this.props.className)){
              props={className:this.props.className.filter(x=>x!==cls)};
              if(props.className.length===this.props.className.length){
                return;
              }
              this.updateProps(props);
            }else if(this.props.className===cls){
              props={className:null};
              this.updateProps(props);
            }
          }
          hasClassName(cls){
            let props;
            if(Array.isArray(this.props.className)){
              return this.props.className.filter(x=>x===cls).length===1;
            }
            return this.props.className===cls;
          }
          connect(signal,callback){
            console.log("signal connect:"+signal._event_name,callback);
            const ref={element:this,signal:signal,callback:callback};
            signal._slots.push(ref);
            this.slots.push(ref);
          }
          disconnect(signal){
            console.log("signal disconnect:"+signal._event_name);
          }
          getDomNode(){
            return this._fiber&&this._fiber.dom;
          }
          isMounted(){
            return this._fiber!==null;
          }
        }
        class TextElement extends DomElement {
          constructor(text,props={}){
            super("TEXT_ELEMENT",{'nodeValue':text,...props},[]);
          }
          setText(text){
            this.props={'nodeValue':text};
            this.update();
          }
          getText(){
            return this.props.nodeValue;
          }
        }
        class LinkElement extends DomElement {
          constructor(text,url){
            super("div",{className:LinkElement.style.link,title:url},[new TextElement(
                                  text)]);
            this.state={url};
          }
          onClick(){
            if(this.state.url.startsWith('http')){
              window.open(this.state.url,'_blank');
            }else{
              history.pushState({},"",this.state.url);
            }
          }
        }
        LinkElement.style={link:'dcs-1b463782-0'};
        class ListElement extends DomElement {
          constructor(){
            super("ul",{},[]);
          }
        }
        class ListItemElement extends DomElement {
          constructor(item){
            super("li",{},[item]);
          }
        }
        class HeaderElement extends DomElement {
          constructor(text=""){
            super("h1",{},[]);
            this.node=this.appendChild(new TextElement(text));
          }
          setText(text){
            this.node.setText(text);
          }
        }
        class ButtonElement extends DomElement {
          constructor(text,onClick){
            super("button",{'onClick':onClick},[new TextElement(text)]);
          }
          setText(text){
            this.children[0].setText(text);
          }
          getText(){
            return this.children[0].props.nodeValue;
          }
        }
        class TextInputElement extends DomElement {
          constructor(text,_,submit_callback){
            super("input",{value:text,type:"text"},[]);
            this.textChanged=Signal(this,'textChanged');
            this.attrs={submit_callback};
          }
          setText(text){
            this.updateProps({value:text});
            this.textChanged.emit(this.props);
          }
          getText(){
            return this.props.value;
          }
          onChange(event){
            this.updateProps({value:event.target.value},false);
            this.textChanged.emit(this.props);
          }
          onPaste(event){
            this.updateProps({value:event.target.value},false);
            this.textChanged.emit(this.props);
          }
          onKeyUp(event){
            this.updateProps({value:event.target.value},false);
            this.textChanged.emit(this.props);
            if(event.key=="Enter"){
              if(this.attrs.submit_callback){
                this.attrs.submit_callback(this.props.value);
              }
            }
          }
        }
        class NumberInputElement extends DomElement {
          constructor(value){
            super("input",{value:value,type:"number"},[]);
            this.valueChanged=Signal(this,'valueChanged');
          }
          onChange(event){
            this.updateProps({value:parseInt(event.target.value,10)},false);
            this.valueChanged.emit(this.props);
          }
          onPaste(event){
            this.updateProps({value:parseInt(event.target.value,10)},false);
            this.valueChanged.emit(this.props);
          }
          onKeyUp(event){
            this.updateProps({value:parseInt(event.target.value,10)},false);
            this.valueChanged.emit(this.props);
          }
          onInput(event){
            this.updateProps({value:parseInt(event.target.value,10)},false);
            this.valueChanged.emit(this.props);
          }
        }
        function swap(nodeA,nodeB){
          if(!nodeA||!nodeB){
            return;
          }
          const parentA=nodeA.parentNode;
          const siblingA=nodeA.nextSibling===nodeB?nodeA:nodeA.nextSibling;
          nodeB.parentNode.insertBefore(nodeA,nodeB);
          parentA.insertBefore(nodeB,siblingA);
        }
        function isAbove(nodeA,nodeB){
          if(!nodeA||!nodeB){
            return false;
          }
          const rectA=nodeA.getBoundingClientRect();
          const rectB=nodeB.getBoundingClientRect();
          return(rectA.top+rectA.height/2<rectB.top+rectB.height/2);
        }
        function childIndex(node){
          let count=0;
          while((node=node.previousSibling)!=null){
            count++;
          }
          return count;
        }
        const placeholder='dcs-1b463782-1';
        class DraggableListItem extends DomElement {
          constructor(){
            super("div",{},[]);
          }
          onTouchStart(event){
            this.attrs.parent.handleChildDragBegin(this,event);
          }
          onTouchMove(event){
            this.attrs.parent.handleChildDragMove(this,event);
          }
          onTouchEnd(event){
            this.attrs.parent.handleChildDragEnd(this,{target:this.getDomNode()});
            
          }
          onTouchCancel(event){
            this.attrs.parent.handleChildDragEnd(this,{target:this.getDomNode()});
            
          }
          onMouseDown(event){
            this.attrs.parent.handleChildDragBegin(this,event);
          }
          onMouseMove(event){
            this.attrs.parent.handleChildDragMove(this,event);
          }
          onMouseLeave(event){
            this.attrs.parent.handleChildDragEnd(this,event);
          }
          onMouseUp(event){
            this.attrs.parent.handleChildDragEnd(this,event);
          }
        }
        class DraggableList extends DomElement {
          constructor(){
            super("div",{},[]);
            this.attrs={x:null,y:null,placeholder:null,placeholderClassName:placeholder,
                          draggingEle:null,isDraggingStarted:false,indexStart:-1,lockX:true};
            
          }
          setPlaceholderClassName(className){
            this.attrs.placeholderClassName=className;
          }
          handleChildDragBegin(child,event){
            event.preventDefault();
            if(!!this.attrs.draggingEle){
              this.handleChildDragCancel();
              return;
            }
            let evt=(((event)||{}).touches||((((event)||{}).originalEvent)||{}).touches);
            
            if(evt){
              event=evt[0];
            }
            this.attrs.draggingEle=child.getDomNode();
            this.attrs.indexStart=childIndex(this.attrs.draggingEle);
            const rect=this.attrs.draggingEle.getBoundingClientRect();
            this.attrs.x=event.clientX-rect.left;
            this.attrs.y=event.pageY-rect.top;
          }
          handleChildDragMove(child,event){
            if(!this.attrs.draggingEle||this.attrs.draggingEle!==child.getDomNode(
                            )){
              return;
            }
            event.preventDefault();
            let evt=(((event)||{}).touches||((((event)||{}).originalEvent)||{}).touches);
            
            if(evt){
              event=evt[0];
            }
            const draggingRect=this.attrs.draggingEle.getBoundingClientRect();
            if(!this.attrs.isDraggingStarted){
              this.attrs.isDraggingStarted=true;
              this.attrs.placeholder=document.createElement('div');
              this.attrs.placeholder.classList.add(this.attrs.placeholderClassName);
              
              this.attrs.draggingEle.parentNode.insertBefore(this.attrs.placeholder,
                              this.attrs.draggingEle.nextSibling);
              this.attrs.placeholder.style.height=`${draggingRect.height}px`;
            }
            this.attrs.draggingEle.style.position='absolute';
            let ypos=event.pageY-this.attrs.y+window.scrollY;
            this.attrs.draggingEle.style.top=`${ypos}px`;
            if(!this.attrs.lockX){
              this.attrs.draggingEle.style.left=`${event.pageX-this.attrs.x}px`;
            }
            const prevEle=this.attrs.draggingEle.previousElementSibling;
            const nextEle=this.attrs.placeholder.nextElementSibling;
            if(prevEle&&isAbove(this.attrs.draggingEle,prevEle)){
              swap(this.attrs.placeholder,this.attrs.draggingEle);
              swap(this.attrs.placeholder,prevEle);
              return;
            }
            if(nextEle&&isAbove(nextEle,this.attrs.draggingEle)){
              swap(nextEle,this.attrs.placeholder);
              swap(nextEle,this.attrs.draggingEle);
            }
          }
          handleChildDragEnd(child,event){
            if(!this.attrs.draggingEle||this.attrs.draggingEle!==child.getDomNode(
                            )){
              return;
            }
            this.handleChildDragCancel();
          }
          handleChildDragCancel(){
            this.attrs.placeholder&&this.attrs.placeholder.parentNode.removeChild(
                          this.attrs.placeholder);
            this.attrs.draggingEle.style.removeProperty('top');
            this.attrs.draggingEle.style.removeProperty('left');
            this.attrs.draggingEle.style.removeProperty('position');
            const indexEnd=childIndex(this.attrs.draggingEle);
            this.updateModel(this.attrs.indexStart,indexEnd);
            this.attrs.x=null;
            this.attrs.y=null;
            this.attrs.draggingEle=null;
            this.attrs.isDraggingStarted=false;
          }
          updateModel(indexStart,indexEnd){
            this.children.splice(indexEnd,0,this.children.splice(indexStart,1)[0]);
            
          }
        }
        return[ButtonElement,DomElement,DraggableList,DraggableListItem,HeaderElement,
                  LinkElement,ListElement,ListItemElement,NumberInputElement,Signal,TextElement,
                  TextInputElement];
      })();
    const[]=(function(){
        history.locationChanged=Signal(null,"locationChanged");
        history.states=[{state:{},title:null,path:window.location.href}];
        history.forward_states=[];
        history._pushState=history.pushState;
        history.pushState=(state,title,path)=>{
          history._pushState(state,title,path);
          history.locationChanged.emit({path:location.pathname});
          history.forward_states=[];
          history.states.push({state,title,path});
        };
        history.goBack=()=>{
          if(history.states.length<2){
            return false;
          }
          const state=history.states.pop();
          history.forward_states.splice(0,0,state);
          const new_state=history.states[history.states.length-1];
          history._pushState(new_state.state,new_state.title,new_state.path);
          history.locationChanged.emit({path:location.pathname});
          return true;
        };
        window.addEventListener('popstate',(event)=>{
            history.locationChanged.emit({path:location.pathname});
          });
        return[];
      })();
    const[AuthenticatedRouter,Router,locationMatch,patternCompile,patternToRegexp]=(
          function(){
        function patternCompile(pattern){
          const arr=pattern.split('/');
          let tokens=[];
          for(let i=1;i<arr.length;i++)
          {
            let part=arr[i];
            if(part.startsWith(':')){
              if(part.endsWith('?')){
                tokens.push({param:true,name:part.substr(1,part.length-2)});
              }else if(part.endsWith('+')){
                tokens.push({param:true,name:part.substr(1,part.length-2)});
              }else if(part.endsWith('*')){
                tokens.push({param:true,name:part.substr(1,part.length-2)});
              }else{
                tokens.push({param:true,name:part.substr(1)});
              }
            }else{
              tokens.push({param:false,value:part});
            }
          }
          return(items,query_items)=>{
            let location='';
            for(let i=0;i<tokens.length;i++)
            {
              location+='/';
              if(tokens[i].param){
                location+=items[tokens[i].name];
              }else{
                location+=tokens[i].value;
              }
            }
            if(!!query_items){
              location+=util.serializeParameters(query_items);
            }
            return location;
          };
        }
        function patternToRegexp(pattern,exact=true){
          const arr=pattern.split('/');
          let re="^";
          let tokens=[];
          for(let i=exact?1:0;i<arr.length;i++)
          {
            let part=arr[i];
            if(i==0&&exact===false){

            }else{
              re+="\\/";
            }
            if(part.startsWith(':')){
              if(part.endsWith('?')){
                tokens.push(part.substr(1,part.length-2));
                re+="([^\\/]*)";
              }else if(part.endsWith('+')){
                tokens.push(part.substr(1,part.length-2));
                re+="?(.+)";
              }else if(part.endsWith('*')){
                tokens.push(part.substr(1,part.length-2));
                re+="?(.*)";
              }else{
                tokens.push(part.substr(1));
                re+="([^\\/]+)";
              }
            }else{
              re+=part;
            }
          }
          if(re!=="^\\/"){
            re+="\\/?";
          }
          re+="$";
          return{re:new RegExp(re,"i"),text:re,tokens};
        }
        function locationMatch(obj,location){
          obj.re.lastIndex=0;
          let arr=location.match(obj.re);
          if(arr==null){
            return null;
          }
          let result={};
          for(let i=1;i<arr.length;i++)
          {
            result[obj.tokens[i-1]]=arr[i];
          }
          return result;
        }
        function patternMatch(pattern,location){
          return locationMatch(patternToRegexp(pattern),location);
        }
        class Router{
          constructor(container,default_callback){
            if(!container){
              throw'invalid container';
            }
            this.container=container;
            this.default_callback=default_callback;
            this.routes=[];
            this.current_index=-2;
            this.current_location=null;
          }
          handleLocationChanged(location){
            let index=0;
            while(index<this.routes.length){
              const item=this.routes[index];
              const match=locationMatch(item.re,location);
              if(match!==null){
                let fn=(element)=>this.setElement(index,location,match,element);
                if(this.doRoute(item,fn,match)){
                  return;
                }
              }
              index+=1;
            }
            let fn=(element)=>this.setElement(-1,location,null,element);
            this.default_callback(fn);
            return;
          }
          doRoute(item,fn,match){
            item.callback(fn,match);
            return true;
          }
          setElement(index,location,match,element){
            if(!!element){
              if(index!=this.current_index){
                this.container.children=[element];
                this.container.update();
              }
              if(this.current_location!==location){
                this.setMatch(match);
                element.updateState({match:match});
              }
              this.current_index=index;
            }else{
              this.container.children=[];
              this.current_index=-1;
              this.container.update();
            }
            this.current_location=location;
          }
          addRoute(pattern,callback){
            const re=patternToRegexp(pattern);
            this.routes.push({pattern,callback,re});
          }
          setDefaultRoute(callback){
            this.default_callback=callback;
          }
          setMatch(match){

          }
          clear(){
            this.container.children=[];
            this.current_index=-1;
            this.current_location=null;
            this.container.update();
          }
        }
        class AuthenticatedRouter extends Router {
          constructor(container,route_list,default_callback){
            super(container,route_list,default_callback);
            this.authenticated=false;
          }
          doRoute(item,fn,match){
            let has_auth=this.isAuthenticated();
            if(item.auth===true&&item.noauth===undefined){
              if(!!has_auth){
                item.callback(fn,match);
                return true;
              }else if(item.fallback!==undefined){
                history.pushState({},"",item.fallback);
                return true;
              }
            }
            if(item.auth===undefined&&item.noauth===true){
              console.log(item,has_auth);
              if(!has_auth){
                item.callback(fn,match);
                return true;
              }else if(item.fallback!==undefined){
                history.pushState({},"",item.fallback);
                return true;
              }
            }
            if(item.auth===undefined&&item.noauth===undefined){
              item.callback(fn,match);
              return true;
            }
            return false;
          }
          isAuthenticated(){
            return this.authenticated;
          }
          setAuthenticated(value){
            this.authenticated=!!value;
          }
          addAuthRoute(pattern,callback,fallback){
            const re=patternToRegexp(pattern);
            this.routes.push({pattern,callback,auth:true,fallback,re});
          }
          addNoAuthRoute(pattern,callback,fallback){
            const re=patternToRegexp(pattern);
            this.routes.push({pattern,callback,noauth:true,fallback,re});
          }
        }
        return[AuthenticatedRouter,Router,locationMatch,patternCompile,patternToRegexp];
        
      })();
    const[downloadFile,uploadFile]=(function(){
        function saveBlob(blob,fileName){
          let a=document.createElement('a');
          a.href=window.URL.createObjectURL(blob);
          a.download=fileName;
          a.dispatchEvent(new MouseEvent('click'));
        }
        function downloadFile(url,headers={},params={},success=null,failure=null){
        
          const postData=new FormData();
          const queryString=util.serializeParameters(params);
          const xhr=new XMLHttpRequest();
          xhr.open('GET',url+queryString);
          for(let key in headers){
            xhr.setRequestHeader(key,headers[key]);
          }
          xhr.responseType='blob';
          xhr.onload=function(this_,event_){
            let blob=this_.target.response;
            if(!blob||this_.target.status!=200){
              if(failure!==null){
                failure({status:this_.target.status,blob});
              }
            }else{
              let contentDispo=xhr.getResponseHeader('Content-Disposition');
              console.log(xhr);
              let fileName;
              if(contentDispo!==null){
                fileName=contentDispo.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)[
                                1];
              }
              if(!fileName){
                console.error("filename not found in xhr request header 'Content-Disposition'");
                
                let parts;
                parts=xhr.responseURL.split('/');
                parts=parts[parts.length-1].split('?');
                fileName=parts[0]||'resource.bin';
              }
              saveBlob(blob,fileName);
              if(success!==null){
                success({url,fileName,blob});
              }
            }
          };
          xhr.send(postData);
        }
        function _uploadFileImpl(elem,urlbase,headers={},params={},success=null,failure=null,
                  progress=null){
          let queryString=util.serializeParameters(params);
          let arrayLength=elem.files.length;
          for(let i=0;i<arrayLength;i++)
          {
            let file=elem.files[i];
            let bytesTransfered=0;
            let url;
            if(urlbase.endsWith('/')){
              url=urlbase+file.name;
            }else{
              url=urlbase+'/'+file.name;
            }
            url+=queryString;
            let xhr=new XMLHttpRequest();
            xhr.open('POST',url,true);
            for(let key in headers){
              xhr.setRequestHeader(key,headers[key]);
            }
            xhr.upload.onprogress=function(event){
              if(event.lengthComputable){
                if(progress!==null){
                  bytesTransfered=event.loaded;
                  progress({bytesTransfered,fileSize:file.size,fileName:file.name,
                                          finished:false});
                }
              }
            };
            xhr.onreadystatechange=function(){
              if(xhr.readyState==4&&xhr.status==200){
                if(success!==null){
                  let params={fileName:file.name,url,lastModified:file.lastModified,
                                      size:file.size,type:file.type};
                  success(params);
                  if(progress!==null){
                    progress({bytesTransfered:file.size,fileSize:file.size,fileName:file.name,
                                              finished:true});
                  }
                }
              }else if(xhr.status>=400){
                if(failure!==null){
                  let params={fileName:file.name,url,status:xhr.status};
                  failure(params);
                  if(progress!==null){
                    progress({bytesTransfered,fileSize:file.size,fileName:file.name,
                                              finished:true});
                  }
                }
              }else{
                console.log("xhr status changed: "+xhr.status);
              }
            };
            if(progress!==null){
              progress({bytesTransfered,fileSize:file.size,fileName:file.name,finished:false,
                                  first:true});
            }
            let fd=new FormData();
            fd.append('upload',file);
            xhr.send(fd);
          }
        }
        function uploadFile(urlbase,headers={},params={},success=null,failure=null,
                  progress=null){
          let element=document.createElement('input');
          element.type='file';
          element.hidden=true;
          element.onchange=(event)=>{
            _uploadFileImpl(element,urlbase,headers,params,success,failure,progress);
            
          };
          element.dispatchEvent(new MouseEvent('click'));
        }
        return[downloadFile,uploadFile];
      })();
    const[OSName,platform]=(function(){
        let nVer=navigator.appVersion;
        let nAgt=navigator.userAgent;
        let browserName=navigator.appName;
        let fullVersion=''+parseFloat(navigator.appVersion);
        let majorVersion=parseInt(navigator.appVersion,10);
        let nameOffset,verOffset,ix;
        if((verOffset=nAgt.indexOf("Opera"))!=-1){
          browserName="Opera";
          fullVersion=nAgt.substring(verOffset+6);
          if((verOffset=nAgt.indexOf("Version"))!=-1){
            fullVersion=nAgt.substring(verOffset+8);
          }
        }else if((verOffset=nAgt.indexOf("MSIE"))!=-1){
          browserName="Microsoft Internet Explorer";
          fullVersion=nAgt.substring(verOffset+5);
        }else if((verOffset=nAgt.indexOf("Chrome"))!=-1){
          browserName="Chrome";
          fullVersion=nAgt.substring(verOffset+7);
        }else if((verOffset=nAgt.indexOf("Safari"))!=-1){
          browserName="Safari";
          fullVersion=nAgt.substring(verOffset+7);
          if((verOffset=nAgt.indexOf("Version"))!=-1){
            fullVersion=nAgt.substring(verOffset+8);
          }
        }else if((verOffset=nAgt.indexOf("Firefox"))!=-1){
          browserName="Firefox";
          fullVersion=nAgt.substring(verOffset+8);
        }else if((nameOffset=nAgt.lastIndexOf(' ')+1)<(verOffset=nAgt.lastIndexOf(
                          '/'))){
          browserName=nAgt.substring(nameOffset,verOffset);
          fullVersion=nAgt.substring(verOffset+1);
          if(browserName.toLowerCase()==browserName.toUpperCase()){
            browserName=navigator.appName;
          }
        }
        if((ix=fullVersion.indexOf(";"))!=-1){
          fullVersion=fullVersion.substring(0,ix);
        }
        if((ix=fullVersion.indexOf(" "))!=-1){
          fullVersion=fullVersion.substring(0,ix);
        }
        majorVersion=parseInt(''+fullVersion,10);
        if(isNaN(majorVersion)){
          fullVersion=''+parseFloat(navigator.appVersion);
          majorVersion=parseInt(navigator.appVersion,10);
        }
        let OSName="Unknown OS";
        if(navigator.appVersion.indexOf("Win")!=-1){
          OSName="Windows";
        }
        if(navigator.appVersion.indexOf("Mac")!=-1){
          OSName="MacOS";
        }
        if(navigator.appVersion.indexOf("X11")!=-1){
          OSName="UNIX";
        }
        if(navigator.appVersion.indexOf("Linux")!=-1){
          OSName="Linux";
        }
        function getDefaultFontSize(parentElement){
          parentElement=parentElement||document.body;
          let div=document.createElement('div');
          div.style.width="1000em";
          parentElement.appendChild(div);
          let pixels=div.offsetWidth/1000;
          parentElement.removeChild(div);
          return pixels;
        }
        const isMobile={Android:function(){
            return navigator.userAgent.match(/Android/i);
          },BlackBerry:function(){
            return navigator.userAgent.match(/BlackBerry/i);
          },iOS:function(){
            return navigator.userAgent.match(/iPhone|iPad|iPod/i);
          },Opera:function(){
            return navigator.userAgent.match(/Opera Mini/i);
          },Windows:function(){
            return navigator.userAgent.match(/IEMobile/i)||navigator.userAgent.match(
                          /WPDesktop/i);
          },any:function(){
            return(isMobile.Android()||isMobile.BlackBerry()||isMobile.iOS()||isMobile.Opera(
                            )||isMobile.Windows());
          }};
        const platform={OSName,browser:browserName,fullVersion,majorVersion,appName:navigator.appName,
                  userAgent:navigator.userAgent,platform:build_platform||'web',isAndroid:build_platform==='android',
                  isMobile:(!!isMobile.any())};
        console.log(platform);
        return[OSName,platform];
      })();
    const[render,render_update]=(function(){
        let workstack=[];
        let deletions=[];
        let deletions_removed=new Set();
        let updatequeue=[];
        let wipRoot=null;
        let currentRoot=null;
        let workLoopActive=false;
        let workCounter=0;
        function render(container,element){
          wipRoot={type:"ROOT",dom:container,props:{},children:[element],_fibers:[
                        ],alternate:currentRoot};
          workstack.push(wipRoot);
          if(!workLoopActive){
            workLoopActive=true;
            setTimeout(workLoop,0);
          }
        }
        function render_update(element){
          if(!element.dirty&&element._fiber!==null){
            element.dirty=true;
            const fiber={effect:'UPDATE',children:[element],_fibers:[],alternate:null,
                          partial:true};
            updatequeue.push(fiber);
          }
          if(!workLoopActive){
            workLoopActive=true;
            setTimeout(workLoop,0);
          }
        }
        DomElement.prototype._update=render_update;
        function workLoop(deadline=null){
          let shouldYield=false;
          const initialWorkLength=workstack.length;
          const initialUpdateLength=updatequeue.length;
          let friendly=deadline!=null;
          let initial_delay=0;
          try{
            if(!!friendly){
              initial_delay=deadline.timeRemaining();
              while(!shouldYield){
                while(workstack.length>0&&!shouldYield){
                  let unit=workstack.pop();
                  performUnitOfWork(unit);
                  shouldYield=deadline.timeRemaining()<1;
                }
                if(workstack.length==0&&wipRoot){
                  commitRoot();
                }
                if(workstack.length==0&&updatequeue.length>0&&!wipRoot){
                  wipRoot=updatequeue[0];
                  workstack.push(wipRoot);
                  updatequeue.shift();
                }
                shouldYield=deadline.timeRemaining()<1;
              }
            }else{
              while(1){
                while(workstack.length>0){
                  let unit=workstack.pop();
                  performUnitOfWork(unit);
                }
                if(wipRoot){
                  commitRoot();
                }
                if(updatequeue.length>0&&!wipRoot){
                  wipRoot=updatequeue[0];
                  workstack.push(wipRoot);
                  updatequeue.shift();
                }else{
                  break;
                }
              }
            }
          }catch(e){
            console.error("unhandled workloop exception: "+e.message);
          };
          let debug=workstack.length>1||updatequeue.length>1;
          if(!!debug){
            console.warn("workloop failed to finish",initial_delay,":",initialWorkLength,
                          '->',workstack.length,initialUpdateLength,'->',updatequeue.length);
            
            if(!friendly){
              setTimeout(workLoop,50);
            }else{
              requestIdleCallback(workLoop);
            }
          }else{
            workLoopActive=false;
          }
        }
        function performUnitOfWork(fiber){
          if(!fiber.dom&&fiber.effect=='CREATE'){
            fiber.dom=createDomNode(fiber);
          }
          reconcileChildren(fiber);
        }
        function reconcileChildren(parentFiber){
          workCounter+=1;
          const oldParentFiber=parentFiber.alternate;
          if(!!oldParentFiber){
            oldParentFiber.children.forEach(child=>{
                child._delete=true;
              });
          }
          let prev=parentFiber;
          while(prev.next){
            prev=prev.next;
          }
          parentFiber.children.forEach((element,index)=>{
              if(!element||!element.type){
                console.error(`${parentFiber.element.props.id}: undefined child element at index ${index} `);
                
                return;
              }
              const oldFiber=element._fiber;
              element._delete=false;
              const oldIndex=oldFiber?oldFiber.index:index;
              if(parentFiber.partial){
                index=oldIndex;
              }
              let effect;
              if(!!oldFiber){
                if(oldIndex==index&&element.dirty===false){
                  return;
                }else{
                  effect='UPDATE';
                }
              }else{
                effect='CREATE';
              }
              element.dirty=false;
              const newFiber={type:element.type,effect:effect,props:{...element.props},
                              children:element.children.slice(),_fibers:[],parent:(parentFiber.partial&&oldFiber)?oldFiber.parent:parentFiber,
                              alternate:oldFiber,dom:oldFiber?oldFiber.dom:null,signals:element.signals,
                              element:element,index:index,oldIndex:oldIndex};
              if(!newFiber.parent.dom){
                console.error(`element parent is not mounted id: ${element.props.id} effect: ${effect}`);
                
                return;
              }
              if(newFiber.props.style){
                console.warn("unsafe use of inline style: ",newFiber.type,element.props.id,
                                  newFiber.props.style);
              }
              if(typeof(newFiber.props.style)==='object'){
                newFiber.props.style=util.object2style(newFiber.props.style);
              }
              if(Array.isArray(newFiber.props.className)){
                newFiber.props.className=newFiber.props.className.join(' ');
              }
              element._fiber=newFiber;
              parentFiber._fibers.push(newFiber);
              prev.next=newFiber;
              prev=newFiber;
              workstack.push(newFiber);
            });
          if(!!oldParentFiber){
            oldParentFiber.children.forEach(child=>{
                if(child._delete){
                  deletions.push(child._fiber);
                }
              });
          }
        }
        function commitRoot(){
          deletions_removed=new Set();
          deletions.forEach(removeDomNode);
          if(deletions_removed.size>0){
            deletions_removed.forEach(elem=>{
                requestIdleCallback(elem.elementUnmounted.bind(elem));
              });
          }
          let unit=wipRoot.next;
          let next;
          while(unit){
            commitWork(unit);
            next=unit.next;
            unit.next=null;
            unit=next;
          }
          currentRoot=wipRoot;
          wipRoot=null;
          deletions=[];
        }
        function commitWork(fiber){
          const parentDom=fiber.parent.dom;
          if(!parentDom){
            console.warn(`element has no parent. effect: ${fiber.effect}`);
            return;
          }
          if(fiber.effect==='CREATE'){
            const length=parentDom.children.length;
            const position=fiber.index;
            if(length==position){
              parentDom.appendChild(fiber.dom);
            }else{
              parentDom.insertBefore(fiber.dom,parentDom.children[position]);
            }
            if(fiber.element.elementMounted){
              requestIdleCallback(fiber.element.elementMounted.bind(fiber.element));
              
            }
          }else if(fiber.effect==='UPDATE'){
            fiber.alternate.alternate=null;
            updateDomNode(fiber);
          }else if(fiber.effect==='DELETE'){
            fiber.alternate.alternate=null;
            removeDomNode(fiber);
          }
        }
        const isEvent=key=>key.startsWith("on");
        const isProp=key=>!isEvent(key);
        const isCreate=(prev,next)=>key=>(key in next&&!(key in prev));
        const isUpdate=(prev,next)=>key=>(key in prev&&key in next&&prev[key]!==next[
                    key]);
        const isDelete=(prev,next)=>key=>!(key in next);
        function createDomNode(fiber){
          const dom=fiber.type=="TEXT_ELEMENT"?document.createTextNode(""):document.createElement(
                      fiber.type);
          Object.keys(fiber.props).filter(isEvent).forEach(key=>{
              const event=key.toLowerCase().substring(2);
              dom.addEventListener(event,fiber.props[key]);
            });
          Object.keys(fiber.props).filter(isProp).forEach(key=>{
              dom[key]=fiber.props[key];
            });
          return dom;
        }
        function updateDomNode(fiber){
          const dom=fiber.dom;
          const parentDom=fiber.parent.dom;
          const oldProps=fiber.alternate.props;
          const newProps=fiber.props;
          if(!dom){
            console.log("fiber does not contain a dom");
            return;
          }
          if(fiber.oldIndex!=fiber.index&&parentDom){
            if(parentDom.children[fiber.index]!==dom){
              parentDom.removeChild(fiber.dom);
              parentDom.insertBefore(fiber.dom,parentDom.children[fiber.index]);
            }
          }
          Object.keys(oldProps).filter(isEvent).filter(key=>isUpdate(oldProps,newProps)(
                          key)||isDelete(oldProps,newProps)(key)).forEach(key=>{
              const event=key.toLowerCase().substring(2);
              dom.removeEventListener(event,oldProps[key]);
            });
          Object.keys(newProps).filter(isEvent).filter(key=>isCreate(oldProps,newProps)(
                          key)||isUpdate(oldProps,newProps)(key)).forEach(key=>{
              const event=key.toLowerCase().substring(2);
              dom.addEventListener(event,newProps[key]);
            });
          Object.keys(oldProps).filter(isProp).filter(isDelete(oldProps,newProps)).forEach(
                      key=>{
              dom[key]="";
            });
          Object.keys(newProps).filter(isProp).filter(key=>isCreate(oldProps,newProps)(
                          key)||isUpdate(oldProps,newProps)(key)).forEach(key=>{
              dom[key]=newProps[key];
            });
        }
        function _removeDomNode_elementFixUp(element){
          if(element.elementUnmounted){
            deletions_removed.add(element);
          }
          element.children.forEach(child=>{
              child._fiber=null;
              _removeDomNode_elementFixUp(child);
            });
        }
        function removeDomNode(fiber){
          if(fiber.dom){
            if(fiber.dom.parentNode){
              fiber.dom.parentNode.removeChild(fiber.dom);
            }
          }else{
            console.error("failed to delete",fiber.element.type);
          }
          fiber.dom=null;
          fiber.element._fiber=null;
          fiber.alternate=null;
          _removeDomNode_elementFixUp(fiber.element);
        }
        return[render,render_update];
      })();
    return{AuthenticatedRouter,ButtonElement,DomElement,DraggableList,DraggableListItem,
          HeaderElement,LinkElement,ListElement,ListItemElement,NumberInputElement,OSName,
          Router,Signal,StyleSheet,TextElement,TextInputElement,build_platform,downloadFile,
          env,getStyleSheet,locationMatch,parseParameters,patternCompile,patternToRegexp,
          platform,render,render_update,uploadFile,util};
  })();
api.requests=(function(){
    "use strict";
    function get_text(url,parameters){
      if(parameters===undefined){
        parameters={};
      }
      parameters.method="GET";
      return fetch(url,parameters).then((response)=>{
          return response.text();
        });
    }
    function get_json(url,parameters){
      if(parameters===undefined){
        parameters={};
      }
      parameters.method="GET";
      return fetch(url,parameters).then((response)=>{
          if(!response.ok){
            throw response;
          }
          return response.json();
        });
    }
    function post(url,payload,parameters){
      if(parameters===undefined){
        parameters={};
      }
      if(parameters.headers===undefined){
        parameters.headers={};
      }
      parameters.method="POST";
      parameters.body=payload;
      return fetch(url,parameters).then((response)=>{
          return response.json();
        });
    }
    function post_json(url,payload,parameters){
      if(parameters===undefined){
        parameters={};
      }
      if(parameters.headers===undefined){
        parameters.headers={};
      }
      parameters.method="POST";
      parameters.headers['Content-Type']="application/json";
      parameters.body=JSON.stringify(payload);
      return fetch(url,parameters).then((response)=>{
          return response.json();
        });
    }
    function put_json(url,payload,parameters){
      if(parameters===undefined){
        parameters={};
      }
      if(parameters.headers===undefined){
        parameters.headers={};
      }
      parameters.method="PUT";
      parameters.headers['Content-Type']="application/json";
      parameters.body=JSON.stringify(payload);
      return fetch(url,parameters).then((response)=>{
          return response.json();
        });
    }
    return{get_json,get_text,post,post_json,put_json};
  })();
Object.assign(api,(function(api,daedalus){
      "use strict";
      const[clearUserToken,getAuthConfig,getAuthToken,getUsertoken,setUsertoken]=(
              function(){
          let user_token=null;
          function getUsertoken(){
            if(user_token===null){
              const token=LocalStorage.getItem("user_token");
              if(token&&token.length>0){
                user_token=token;
              }
            }
            return user_token;
          }
          function setUsertoken(token){
            LocalStorage.setItem("user_token",token);
            user_token=token;
          }
          function clearUserToken(creds){
            LocalStorage.removeItem("user_token");
            user_token=null;
          }
          function getAuthConfig(){
            return{credentials:'include',headers:{Authorization:user_token}};
          }
          function getAuthToken(){
            return user_token;
          }
          return[clearUserToken,getAuthConfig,getAuthToken,getUsertoken,setUsertoken];
          
        })();
      const[authenticate,env,fsGetPath,fsGetPathContent,fsGetPathContentUrl,fsGetPublicPathUrl,
              fsGetRoots,fsNoteCreate,fsNoteGetContent,fsNoteList,fsNoteSetContent,fsPathPreviewUrl,
              fsPathUrl,fsPublicUriGenerate,fsPublicUriInfo,fsPublicUriRevoke,fsSearch,
              fsUploadFile,libraryDomainInfo,librarySearchForest,librarySong,librarySongAudioUrl,
              queueCreate,queueGetQueue,queuePopulate,queueSetQueue,radioVideoInfo,userDoc,
              validate_token]=(function(){
          const env={baseUrl:(daedalus.env&&daedalus.env.baseUrl)?daedalus.env.baseUrl:""};
          
          env.origin=window.location.origin;
          function authenticate(email,password){
            const url=env.baseUrl+'/api/user/login';
            return api.requests.post_json(url,{email,password});
          }
          function validate_token(token){
            const url=env.baseUrl+'/api/user/token';
            return api.requests.post_json(url,{token});
          }
          function fsGetRoots(){
            const url=env.baseUrl+'/api/fs/roots';
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function fsPathPreviewUrl(root,path){
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs',root,'path',path);
            
            const params=daedalus.util.serializeParameters({preview:'thumb','dl':0,
                              'token':getAuthToken()});
            return url+params;
          }
          function fsPathUrl(root,path,dl){
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs',root,'path',path);
            
            const params=daedalus.util.serializeParameters({'dl':dl,'token':getAuthToken(
                                )});
            return url+params;
          }
          function fsGetPath(root,path){
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs',root,'path',path);
            
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function fsGetPathContent(root,path){
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs',root,'path',path);
            
            const cfg=getAuthConfig();
            return api.requests.get_text(url,cfg);
          }
          function fsGetPathContentUrl(root,path){
            const url=env.origin+daedalus.util.joinpath('/u/storage/preview',root,
                          path);
            return url;
          }
          function fsGetPublicPathUrl(uid,name){
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs/public',uid,name);
            
            return url;
          }
          function fsSearch(root,path,terms,page,limit){
            const params=daedalus.util.serializeParameters({path,terms,page,limit});
            
            const url=env.baseUrl+'/api/fs/'+root+'/search'+params;
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function fsPublicUriGenerate(root,path){
            const url=env.baseUrl+'/api/fs/public/'+root+'/path/'+path;
            const cfg=getAuthConfig();
            return api.requests.put_json(url,{},cfg);
          }
          function fsPublicUriRevoke(root,path){
            const params=daedalus.util.serializeParameters({revoke:true});
            const url=env.baseUrl+'/api/fs/public/'+root+'/path/'+path+params;
            const cfg=getAuthConfig();
            return api.requests.put_json(url,{},cfg);
          }
          function fsPublicUriInfo(uid,name){
            const params=daedalus.util.serializeParameters({info:true});
            const url=env.baseUrl+daedalus.util.joinpath('/api/fs/public',uid,name)+params;
            
            return api.requests.get_json(url);
          }
          function fsNoteList(root,base){
            const params=daedalus.util.serializeParameters({root,base});
            const url=env.baseUrl+'/api/fs/notes'+params;
            const cfg=getAuthConfig();
            cfg.headers['Content-Type']="application/json";
            return api.requests.get_json(url,cfg);
          }
          function fsNoteCreate(root,base,title,content,crypt,password){
            const params=daedalus.util.serializeParameters({root,base,title,crypt});
            
            const url=env.baseUrl+'/api/fs/notes'+params;
            const cfg=getAuthConfig();
            cfg.headers['X-YUE-PASSWORD']=password;
            return api.requests.post(url,content,cfg);
          }
          function fsNoteGetContent(root,base,note_id,crypt,password){
            const params=daedalus.util.serializeParameters({root,base,crypt});
            const url=env.baseUrl+'/api/fs/notes/'+note_id+params;
            const cfg=getAuthConfig();
            cfg.headers['X-YUE-PASSWORD']=password;
            return api.requests.get_text(url,cfg);
          }
          function fsNoteSetContent(root,base,note_id,content,crypt,password){
            const params=daedalus.util.serializeParameters({root,base,crypt});
            const url=env.baseUrl+'/api/fs/notes/'+note_id+params;
            const cfg=getAuthConfig();
            cfg.headers['X-YUE-PASSWORD']=password;
            cfg.headers['Content-Type']='text/plain';
            return api.requests.post(url,content,cfg);
          }
          function queueGetQueue(){
            const url=env.baseUrl+'/api/queue';
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function queueSetQueue(songList){
            const url=env.baseUrl+'/api/queue';
            const cfg=getAuthConfig();
            return api.requests.post_json(url,songList,cfg);
          }
          function queuePopulate(){
            const url=env.baseUrl+'/api/queue/populate';
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function queueCreate(query,limit=50){
            const params=daedalus.util.serializeParameters({query,limit});
            const url=env.baseUrl+'/api/queue/create'+params;
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function librarySongAudioUrl(songId){
            const url=env.baseUrl+`/api/library/${songId}/audio`;
            const params=daedalus.util.serializeParameters({'token':getAuthToken(
                                )});
            return url+params;
          }
          function librarySearchForest(query,showBanished=false){
            const params=daedalus.util.serializeParameters({query,showBanished});
            
            const url=env.baseUrl+'/api/library/forest'+params;
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function librarySong(songId){
            const url=env.baseUrl+'/api/library/'+songId;
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function libraryDomainInfo(){
            const url=env.baseUrl+'/api/library/info';
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function userDoc(hostname){
            const params=daedalus.util.serializeParameters({hostname});
            const url=env.baseUrl+'/api/doc'+params;
            const cfg=getAuthConfig();
            return api.requests.get_text(url,cfg);
          }
          function radioVideoInfo(videoId){
            const params=daedalus.util.serializeParameters({videoId});
            const url=env.baseUrl+'/api/radio/video/info'+params;
            const cfg=getAuthConfig();
            return api.requests.get_json(url,cfg);
          }
          function fsUploadFile(root,path,headers,params,success=null,failure=null,
                      progress=null){
            const urlbase=env.baseUrl+daedalus.util.joinpath('/api/fs',root,'path',
                          path);
            const cfg=getAuthConfig();
            return daedalus.uploadFile(urlbase,headers={...cfg.headers,...headers},
                          params=params,success,failure,progress);
          }
          return[authenticate,env,fsGetPath,fsGetPathContent,fsGetPathContentUrl,
                      fsGetPublicPathUrl,fsGetRoots,fsNoteCreate,fsNoteGetContent,fsNoteList,
                      fsNoteSetContent,fsPathPreviewUrl,fsPathUrl,fsPublicUriGenerate,fsPublicUriInfo,
                      fsPublicUriRevoke,fsSearch,fsUploadFile,libraryDomainInfo,librarySearchForest,
                      librarySong,librarySongAudioUrl,queueCreate,queueGetQueue,queuePopulate,
                      queueSetQueue,radioVideoInfo,userDoc,validate_token];
        })();
      return{authenticate,clearUserToken,env,fsGetPath,fsGetPathContent,fsGetPathContentUrl,
              fsGetPublicPathUrl,fsGetRoots,fsNoteCreate,fsNoteGetContent,fsNoteList,fsNoteSetContent,
              fsPathPreviewUrl,fsPathUrl,fsPublicUriGenerate,fsPublicUriInfo,fsPublicUriRevoke,
              fsSearch,fsUploadFile,getAuthConfig,getAuthToken,getUsertoken,libraryDomainInfo,
              librarySearchForest,librarySong,librarySongAudioUrl,queueCreate,queueGetQueue,
              queuePopulate,queueSetQueue,radioVideoInfo,setUsertoken,userDoc,validate_token};
      
    })(api,daedalus));
resources=(function(daedalus){
    "use strict";
    const platform_prefix=daedalus.platform.isAndroid?"file:///android_asset/site/static/icon/":"/static/icon/";
    
    const svg_icon_names=["album","bolt","create","discard","disc","documents","download",
          "edit","equalizer","externalmedia","file","folder","genre","logout","media_error",
          "media_next","media_pause","media_play","media_prev","media_shuffle","menu",
          "microphone","more","music_note","new_folder","note","open","playlist","preview",
          "rename","return","save","search","search_generic","select","settings","shuffle",
          "sort","upload","volume_0","volume_1","volume_2","volume_4","checkbox_unchecked",
          "checkbox_partial","checkbox_download","checkbox_checked","checkbox_synced",
          "checkbox_not_synced","plus","minus"];
    const svg={};
    svg_icon_names.forEach(name=>{
        svg[name]=platform_prefix+name+".svg";
      });
    return{svg};
  })(daedalus);
components=(function(daedalus,resources){
    "use strict";
    const StyleSheet=daedalus.StyleSheet;
    const DomElement=daedalus.DomElement;
    const ButtonElement=daedalus.ButtonElement;
    const TextElement=daedalus.TextElement;
    const TextInputElement=daedalus.TextInputElement;
    const Router=daedalus.Router;
    const[SvgButtonElement,SvgElement]=(function(){
        const style={svgButton:'dcs-b308e454-0'};
        ;
        class SvgElement extends DomElement {
          constructor(url,props){
            super("img",{src:url,...props},[]);
          }
          onLoad(event){
            console.warn("success loading: ",this.props.src);
          }
          onError(error){
            console.warn("error loading: ",this.props.src,JSON.stringify(error));
            
          }
        }
        class SvgButtonElement extends SvgElement {
          constructor(url,callback){
            super(url,{width:32,height:32,className:style.svgButton});
            this.attrs={callback};
          }
          onClick(event){
            if(this.attrs.callback){
              this.attrs.callback();
            }
          }
          setUrl(url){
            this.props.src=url;
            this.update();
          }
        }
        return[SvgButtonElement,SvgElement];
      })();
    const[SwipeHandler]=(function(){
        class SwipeHandler{
          constructor(parent,callback){
            this.mount(parent);
            this.callback=callback;
            this.xDown=null;
            this.yDown=null;
          }
          mount(dom){
            console.log("mount",dom);
            dom.addEventListener('touchstart',this.handleTouchStart.bind(this),false);
            
            dom.addEventListener('touchmove',this.handleTouchMove.bind(this),false);
            
          }
          getTouches(evt){
            return evt.touches||evt.originalEvent.touches;
          }
          handleTouchStart(evt){
            try{
              const firstTouch=this.getTouches(evt)[0];
              this.xDown=firstTouch.clientX;
              this.yDown=firstTouch.clientY;
              let pt={x:this.xDown,y:this.yDown};
              this.callback(pt,0);
            }catch(e){
              this.callback({x:0,y:0},e);
            };
          }
          handleTouchMove(evt){
            if(!this.xDown||!this.yDown){
              return;
            }
            let xUp=evt.touches[0].clientX;
            let yUp=evt.touches[0].clientY;
            let xDiff=this.xDown-xUp;
            let yDiff=this.yDown-yUp;
            let pt={x:this.xDown,y:this.yDown,xc:xUp,yc:yUp,dx:xDiff,dy:yDiff};
            let direction=0;
            if(Math.abs(xDiff)>Math.abs(yDiff)){
              if(xDiff>0){
                direction=SwipeHandler.LEFT;
              }else{
                direction=SwipeHandler.RIGHT;
              }
            }else{
              if(yDiff>0){
                direction=SwipeHandler.UP;
              }else{
                direction=SwipeHandler.DOWN;
              }
            }
            if(direction!=0){
              this.callback(pt,direction);
            }
            this.xDown=null;
            this.yDown=null;
          }
        }
        SwipeHandler.UP=1;
        SwipeHandler.DOWN=2;
        SwipeHandler.RIGHT=3;
        SwipeHandler.LEFT=4;
        return[SwipeHandler];
      })();
    const[HSpacer,VSpacer]=(function(){
        class HSpacer extends DomElement {
          constructor(width){
            super("div",{},[]);
            this.attrs={width};
          }
          elementMounted(){
            this._setWidth();
          }
          setWidth(width){
            this.attrs.width=width;
            this._setWidth();
          }
          _setWidth(){
            const node=this.getDomNode();
            if(!!node){
              node.style['max-width']=this.attrs.width;
              node.style['min-width']=this.attrs.width;
              node.style['width']=this.attrs.width;
              node.style['max-height']="1px";
              node.style['min-height']="1px";
              node.style['height']="1px";
            }
          }
        }
        class VSpacer extends DomElement {
          constructor(height){
            super("div",{},[]);
            this.attrs={height};
          }
          elementMounted(){
            this._setHeight();
          }
          setHeight(height){
            this.attrs.height=height;
            this._setHeight();
          }
          _setHeight(){
            const node=this.getDomNode();
            if(!!node){
              node.style['max-height']=this.attrs.height;
              node.style['min-height']=this.attrs.height;
              node.style['height']=this.attrs.height;
              node.style['max-width']="1px";
              node.style['min-width']="1px";
              node.style['width']="1px";
            }
          }
        }
        return[HSpacer,VSpacer];
      })();
    const[CheckBoxElement]=(function(){
        const style={chkbox:'dcs-6e53eb54-0'};
        class CheckBoxElement extends SvgElement {
          constructor(callback,initialCheckState){
            super(null,{width:20,height:32,className:style.chkbox});
            if(initialCheckState===undefined){
              throw"error null state: "+initialCheckState;
            }
            this.props.src=this.getStateIcons()[initialCheckState];
            this.attrs={callback,checkState:initialCheckState,initialCheckState};
            
          }
          setCheckState(checkState){
            this.attrs.checkState=checkState;
            this.props.src=this.getStateIcons()[checkState];
            this.update();
          }
          onClick(event){
            this.attrs.callback();
          }
          getStateIcons(){
            return[resources.svg.checkbox_unchecked,resources.svg.checkbox_checked,
                          resources.svg.checkbox_partial];
          }
        }
        CheckBoxElement.UNCHECKED=0;
        CheckBoxElement.CHECKED=1;
        CheckBoxElement.PARTIAL=2;
        return[CheckBoxElement];
      })();
    const[NavMenu]=(function(){
        const style={navMenuShadow:'dcs-eef822cd-0',navMenuShadowHide:'dcs-eef822cd-1',
                  alignRight:'dcs-eef822cd-2',navMenuShadowShow:'dcs-eef822cd-3',navMenu:'dcs-eef822cd-4',
                  navMenuActionContainer:'dcs-eef822cd-5',navMenuHide:'dcs-eef822cd-6',navMenuShow:'dcs-eef822cd-7',
                  navMenuShowFixed:'dcs-eef822cd-8',navMenuHideFixed:'dcs-eef822cd-9',svgDiv:'dcs-eef822cd-10',
                  actionItem:'dcs-eef822cd-11',subActionItem:'dcs-eef822cd-12',header:'dcs-eef822cd-13'};
        
        ;
        ;
        class NavMenuSvgImpl extends DomElement {
          constructor(url,size,props){
            super("img",{src:url,width:size,height:size,...props},[]);
          }
        }
        class NavMenuSvg extends DomElement {
          constructor(url,size=48,props={}){
            super("div",{className:style.svgDiv},[new NavMenuSvgImpl(url,size,props)]);
            
          }
        }
        class NavMenuAction extends DomElement {
          constructor(icon_url,text,callback){
            super("div",{className:style.actionItem},[]);
            this.attrs={callback};
            this.appendChild(new NavMenuSvg(icon_url,48));
            this.appendChild(new TextElement(text));
          }
          onClick(){
            this.attrs.callback();
          }
        }
        class NavMenuSubAction extends DomElement {
          constructor(icon_url,text,callback){
            super("div",{className:[style.actionItem,style.subActionItem]},[]);
            this.attrs={callback};
            this.appendChild(new HSpacer("2em"));
            this.appendChild(new NavMenuSvg(icon_url,32));
            this.appendChild(new TextElement(text));
          }
          onClick(){
            this.attrs.callback();
          }
        }
        class NavMenuHeader extends DomElement {
          constructor(){
            super("div",{className:style.header},[]);
          }
        }
        class NavMenuActionContainer extends DomElement {
          constructor(){
            super("div",{className:style.navMenuActionContainer},[]);
          }
        }
        class NavMenuImpl extends DomElement {
          constructor(){
            super("div",{className:[style.navMenu,style.navMenuHide]},[]);
            this.appendChild(new NavMenuHeader());
            this.attrs={actions:this.appendChild(new NavMenuActionContainer())};
          }
          onClick(event){
            event.stopPropagation();
            return false;
          }
        }
        class NavMenu extends DomElement {
          constructor(){
            super("div",{className:[style.navMenuShadow,style.navMenuShadowHide]},
                          []);
            this.attrs={menu:this.appendChild(new NavMenuImpl(this)),fixed:false};
            
            this.appendChild(new DomElement("div",{className:style.alignRight,onClick:(
                                      event)=>event.stopPropagation()},[]));
            this.attrs.swipe=new SwipeHandler(document,(pt,direction)=>{
                if(direction==SwipeHandler.RIGHT&&pt.x<20){
                  this.show();
                }
              });
          }
          addAction(icon_url,text,callback){
            this.attrs.menu.attrs.actions.appendChild(new NavMenuAction(icon_url,
                              text,callback));
          }
          addSubAction(icon_url,text,callback){
            this.attrs.menu.attrs.actions.appendChild(new NavMenuSubAction(icon_url,
                              text,callback));
          }
          hide(){
            if(this.attrs.fixed){
              return;
            }
            this.attrs.menu.removeClassName(style.navMenuHideFixed);
            this.attrs.menu.removeClassName(style.navMenuShow);
            this.attrs.menu.addClassName(style.navMenuHide);
            this.removeClassName(style.navMenuShadowShow);
            this.addClassName(style.navMenuShadowHide);
          }
          show(){
            if(this.attrs.fixed){
              return;
            }
            this.attrs.menu.removeClassName(style.navMenuHideFixed);
            this.attrs.menu.removeClassName(style.navMenuHide);
            this.attrs.menu.addClassName(style.navMenuShow);
            this.removeClassName(style.navMenuShadowHide);
            this.addClassName(style.navMenuShadowShow);
          }
          showFixed(fixed){
            if(!!fixed){
              this.attrs.menu.removeClassName(style.navMenuHide);
              this.attrs.menu.removeClassName(style.navMenuShow);
              this.attrs.menu.removeClassName(style.navMenuHideFixed);
              this.attrs.menu.addClassName(style.navMenuShowFixed);
              this.addClassName(style.navMenuShadowHide);
              this.removeClassName(style.navMenuShadowShow);
            }else{
              this.attrs.menu.addClassName(style.navMenuHideFixed);
              this.attrs.menu.removeClassName(style.navMenuShowFixed);
              this.addClassName(style.navMenuShadowHide);
              this.removeClassName(style.navMenuShadowShow);
            }
            this.attrs.fixed=fixed;
          }
          isFixed(){
            return this.attrs.fixed;
          }
          toggle(){
            if(this.hasClassName(style.navMenuShadowShow)){
              this.hide();
            }else{
              this.show();
            }
          }
          onClick(){
            this.toggle();
          }
        }
        NavMenu.DEFAULT_WIDTH="300px";
        return[NavMenu];
      })();
    const[MiddleText,MiddleTextLink]=(function(){
        const style={ellideMiddle:'dcs-e3dda9fe-0',ellideMiddleDiv1:'dcs-e3dda9fe-1',
                  ellideMiddleDiv2:'dcs-e3dda9fe-2',ellideMiddleLink:'dcs-e3dda9fe-3'};
        class MiddleText extends DomElement {
          constructor(text){
            super("div",{className:[style.textSpacer]},[]);
            this.pivot=4;
            this.updateProps({className:[style.ellideMiddle,style.textSpacer]});
            this.appendChild(new DomElement("div",{className:style.ellideMiddleDiv1},
                              [new TextElement('')]));
            this.appendChild(new DomElement("div",{className:style.ellideMiddleDiv2},
                              [new TextElement('')]));
            this.setText(text);
          }
          setText(text){
            if(text.length<this.pivot){
              this.children[0].children[0].setText(text);
              this.children[1].children[0].setText("");
            }else{
              text=text.replace(/ /g,"\xa0");
              const idx=text.length-this.pivot;
              const text1=text.substr(0,idx);
              const text2=text.substr(idx,this.pivot);
              this.children[0].children[0].setText(text1);
              this.children[1].children[0].setText(text2);
            }
          }
        }
        class MiddleTextLink extends MiddleText {
          constructor(text,url){
            super(text);
            this.state={url};
            this.props.className.push(style.ellideMiddleLink);
          }
          onClick(){
            if(this.state.url.startsWith('http')){
              window.open(this.state.url,'_blank');
            }else{
              history.pushState({},"",this.state.url);
            }
          }
        }
        return[MiddleText,MiddleTextLink];
      })();
    const[TreeItem,TreeView]=(function(){
        const style={treeView:'dcs-bbed1375-0',treeItem:'dcs-bbed1375-1',treeItemObjectContainer:'dcs-bbed1375-2',
                  treeItemChildContainer:'dcs-bbed1375-3',treeItem0:'dcs-bbed1375-4',treeItemN:'dcs-bbed1375-5',
                  listItemMid:'dcs-bbed1375-6',listItemEnd:'dcs-bbed1375-7',listItemSelected:'dcs-bbed1375-8',
                  treeFooter:'dcs-bbed1375-9'};
        ;
        class SvgMoreElement extends SvgElement {
          constructor(callback){
            super(resources.svg.more,{width:20,height:32,className:style.listItemEnd});
            
            this.state={callback};
          }
          onClick(event){
            this.state.callback();
          }
        }
        const UNSELECTED=0;
        const SELECTED=1;
        const PARTIAL=2;
        class TreeItem extends DomElement {
          constructor(parent,depth,title,obj,selectMode=1,selected=UNSELECTED){
            super("div",{className:[style.treeItem]},[]);
            this.attrs={parent,depth,title,obj,children:null,selected:selected,selectMode,
                          chk:null};
            this.attrs.container1=this.appendChild(new DomElement("div",{className:[
                                      style.treeItemObjectContainer]},[]));
            this.attrs.container2=this.appendChild(new DomElement("div",{className:[
                                      style.treeItemChildContainer]},[]));
            if(this.hasChildren()){
              this.attrs.btn=this.attrs.container1.appendChild(new SvgButtonElement(
                                  resources.svg.plus,this.handleToggleExpand.bind(this)));
            }
            this.attrs.txt=this.attrs.container1.appendChild(new components.MiddleText(
                              title));
            this.attrs.txt.addClassName(style.listItemMid);
            if(selectMode!=TreeItem.SELECTION_MODE_CHECK){
              this.attrs.txt.props.onClick=this.handleToggleSelection.bind(this);
              
            }
            if(selectMode==TreeItem.SELECTION_MODE_CHECK){
              this.setCheckEnabled(this.handleToggleSelection.bind(this),selected);
              
            }
            if(depth===0){
              this.addClassName(style.treeItem0);
            }else{
              this.addClassName(style.treeItemN);
            }
          }
          setMoreCallback(callback){
            this.attrs.more=new SvgMoreElement(callback);
            if(this.attrs.selectMode!=TreeItem.SELECTION_MODE_CHECK){
              this.attrs.container1.appendChild(this.attrs.more);
            }else{
              this.attrs.container1.insertChild(-2,this.attrs.more);
            }
          }
          setCheckEnabled(callback,state){
            this.attrs.chk=this.attrs.container1.appendChild(this.constructCheckbox(
                              callback,state));
          }
          handleToggleExpand(){
            if(!this.hasChildren()){
              return;
            }
            if(this.attrs.children===null){
              this.attrs.children=this.buildChildren(this.attrs.obj);
              if(this.attrs.selected==SELECTED){
                this.attrs.children.forEach(child=>{
                    child.setSelected(SELECTED);
                  });
              }
            }
            if(this.attrs.container2.children.length===0){
              this.attrs.container2.children=this.attrs.children;
              this.attrs.container2.update();
              this.attrs.btn.setUrl(resources.svg.minus);
            }else{
              this.attrs.container2.children=[];
              this.attrs.container2.update();
              this.attrs.btn.setUrl(resources.svg.plus);
            }
          }
          handleToggleSelection(){
            console.log("..");
            let next=(this.attrs.selected!=UNSELECTED)?UNSELECTED:SELECTED;
            this.setSelected(next);
            if(this.attrs.depth>0&&this.attrs.parent!=null){
              this.attrs.parent.handleFixSelection();
            }
          }
          handleFixSelection(){
            let every=this.attrs.children.every(child=>child.attrs.selected==SELECTED);
            
            let some=this.attrs.children.some(child=>child.attrs.selected!=UNSELECTED);
            
            let selected;
            if(every){
              selected=SELECTED;
            }else if(some){
              selected=PARTIAL;
            }else{
              selected=UNSELECTED;
            }
            this.setSelectedInternal(selected);
            if(this.attrs.depth>0&&this.attrs.parent!=null){
              this.attrs.parent.handleFixSelection();
            }
          }
          setSelected(selected){
            if(!!this.attrs.children){
              this.attrs.children.forEach(child=>{
                  child.setSelected(selected);
                });
            }
            this.setSelectedInternal(selected);
          }
          setSelectedInternal(selected){
            this.attrs.selected=selected;
            if(this.attrs.selectMode!=TreeItem.SELECTION_MODE_CHECK){
              if(this.attrs.selected){
                this.attrs.container1.addClassName(style.listItemSelected);
              }else{
                this.attrs.container1.removeClassName(style.listItemSelected);
              }
            }
            if(this.attrs.chk!=null){
              this.attrs.chk.setCheckState(this.attrs.selected);
            }
          }
          countSelected(){
            let sum=0;
            if(this.attrs.children!==null){
              sum+=this.attrs.children.reduce((total,child)=>{
                  total+=child.attrs.selected?1:0;
                  total+=child.countSelected();
                  return total;
                },0);
            }
            sum+=this.attrs.selected?1:0;
            return sum;
          }
          isSelected(){
            return this.attrs.selected;
          }
          hasChildren(){
            return true;
          }
          buildChildren(obj){
            return[];
          }
          constructCheckbox(callback,initialState){
            return new CheckBoxElement(callback,initialState);
          }
        }
        TreeItem.SELECTION_MODE_HIGHLIGHT=1;
        TreeItem.SELECTION_MODE_CHECK=2;
        TreeItem.SELECTION_UNSELECTED=UNSELECTED;
        TreeItem.SELECTION_SELECTED=SELECTED;
        TreeItem.SELECTION_PARTIAL=PARTIAL;
        class TreeView extends DomElement {
          constructor(){
            super("div",{className:style.treeView},[]);
            this.attrs={container:new DomElement("div",{},[]),footer:new DomElement(
                              "div",{className:style.treeFooter},[])};
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.footer);
          }
          reset(){
            this.attrs.container.removeChildren();
          }
          addItem(item){
            this.attrs.container.appendChild(item);
          }
          countSelected(){
            return this.attrs.container.children.reduce((total,child)=>{
                return total+child.countSelected();
              },0);
          }
          selectAll(selected){
            this.attrs.container.children.forEach(child=>{
                child.setSelected(selected);
              });
          }
        }
        return[TreeItem,TreeView];
      })();
    const[MoreMenu]=(function(){
        const style={moreMenuShadow:'dcs-f440542e-0',moreMenu:'dcs-f440542e-1',moreMenuShow:'dcs-f440542e-2',
                  moreMenuHide:'dcs-f440542e-3',moreMenuButton:'dcs-f440542e-4'};
        ;
        ;
        class MoreMenuButton extends DomElement {
          constructor(text,callback){
            super("div",{className:[style.moreMenuButton],onClick:callback},[new TextElement(
                                  text)]);
          }
          setText(text){
            this.children[0].setText(text);
          }
        }
        class MoreMenuImpl extends DomElement {
          constructor(){
            super("div",{className:[style.moreMenu]},[]);
          }
          onClick(event){
            event.stopPropagation();
          }
        }
        class MoreMenu extends DomElement {
          constructor(callback_close){
            super("div",{className:[style.moreMenuShadow,style.moreMenuHide]},[]);
            
            this.attrs={callback_close,impl:this.appendChild(new MoreMenuImpl())};
            
          }
          onClick(){
            this.attrs.callback_close();
          }
          addAction(text,callback){
            this.attrs.impl.appendChild(new MoreMenuButton(text,()=>{
                  callback();
                  this.hide();
                }));
          }
          hide(){
            this.updateProps({className:[style.moreMenuShadow,style.moreMenuHide]});
            
          }
          show(){
            this.updateProps({className:[style.moreMenuShadow,style.moreMenuShow]});
            
          }
        }
        return[MoreMenu];
      })();
    const[NavFooter,NavHeader]=(function(){
        const style={header:'dcs-b0bc04f9-0',footer:'dcs-b0bc04f9-1',headerDiv:'dcs-b0bc04f9-2',
                  toolbar:'dcs-b0bc04f9-3',toolbarInner:'dcs-b0bc04f9-4',toolbarFooter:'dcs-b0bc04f9-5',
                  toolbarFooterInner:'dcs-b0bc04f9-6',toolbar2:'dcs-b0bc04f9-7',toolbarInner2:'dcs-b0bc04f9-8',
                  footerText:'dcs-b0bc04f9-9',toolbar2Start:'dcs-b0bc04f9-10',toolbar2Center:'dcs-b0bc04f9-11',
                  grow:'dcs-b0bc04f9-12',pad:'dcs-b0bc04f9-13'};
        class NavHeader extends DomElement {
          constructor(){
            super("div",{className:style.header},[]);
            this.attrs={div:new DomElement("div",{className:style.headerDiv},[]),
                          toolbar:new DomElement("div",{className:style.toolbar},[]),toolbarInner:new DomElement(
                              "div",{className:style.toolbarInner},[]),rows:[]};
            this.appendChild(this.attrs.div);
            this.attrs.div.appendChild(this.attrs.toolbar);
            this.attrs.toolbar.appendChild(this.attrs.toolbarInner);
          }
          addAction(icon,callback){
            const child=new SvgButtonElement(icon,callback);
            this.attrs.toolbarInner.appendChild(child);
            return child;
          }
          addRow(center=false){
            let outer=new DomElement("div",{className:style.toolbar2},[]);
            if(center){
              outer.addClassName(style.toolbar2Center);
            }else{
              outer.addClassName(style.toolbar2Start);
            }
            let inner=new DomElement("div",{className:style.toolbarInner2},[]);
            outer.appendChild(inner);
            outer.attrs.inner=inner;
            this.attrs.div.appendChild(outer);
            this.attrs.rows.push(outer);
            return inner;
          }
          addRowElement(rowIndex,element){
            this.attrs.rows[rowIndex].children[0].appendChild(element);
          }
          addRowAction(rowIndex,icon,callback){
            this.attrs.rows[rowIndex].children[0].appendChild(new SvgButtonElement(
                              icon,callback));
          }
        }
        class NavFooter extends DomElement {
          constructor(){
            super("div",{className:style.footer},[]);
            this.attrs={div:new DomElement("div",{className:style.headerDiv},[]),
                          toolbar:new DomElement("div",{className:style.toolbarFooter},[]),toolbarInner:new DomElement(
                              "div",{className:style.toolbarFooterInner},[])};
            this.appendChild(this.attrs.div);
            this.attrs.div.appendChild(this.attrs.toolbar);
            this.attrs.toolbar.appendChild(this.attrs.toolbarInner);
          }
          addAction(icon,callback){
            const child=new SvgButtonElement(icon,callback);
            this.attrs.toolbarInner.appendChild(child);
            return child;
          }
          addText(text){
            const child=new TextElement(text);
            this.attrs.toolbarInner.appendChild(new DomElement("div",{className:style.footerText},
                              [child]));
            return child;
          }
        }
        return[NavFooter,NavHeader];
      })();
    const[]=(function(){
        return[];
      })();
    return{CheckBoxElement,HSpacer,MiddleText,MiddleTextLink,MoreMenu,NavFooter,NavHeader,
          NavMenu,SvgButtonElement,SvgElement,SwipeHandler,TreeItem,TreeView,VSpacer};
    
  })(daedalus,resources);
router=(function(api,daedalus){
    "use strict";
    const AuthenticatedRouter=daedalus.AuthenticatedRouter;
    const patternCompile=daedalus.patternCompile;
    let current_match=null;
    class AppRouter extends AuthenticatedRouter {
      isAuthenticated(){
        return api.getUsertoken()!==null;
      }
      setMatch(match){
        current_match=match;
      }
    }
    AppRouter.match=()=>{
      return current_match;
    };
    function navigate(location){
      history.pushState({},"",location);
    }
    const route_urls={userStoragePreview:"/u/storage/preview/:path*",userStorageList:"/u/storage/list/:path*",
          userStorage:"/u/storage/:mode/:path*",userFs:"/u/fs/:path*",userPlaylist:"/u/playlist",
          userSettings:"/u/settings",userNotesEdit:"/u/notes/:noteId/edit",userNotesContent:"/u/notes/:noteId",
          userNotesList:"/u/notes",userLibraryList:"/u/library/list",userLibrarySync:"/u/library/sync",
          userLibrarySavedSearch:"/u/library/saved",userRadio:"/u/radio",userWildCard:"/u/:path*",
          login:"/login",apiDoc:"/doc",publicFile:"/p/:uid/:filename",wildCard:"/:path*"};
    
    const routes={};
    Object.keys(route_urls).map(key=>{
        routes[key]=patternCompile(route_urls[key]);
      });
    return{AppRouter,navigate,route_urls,routes};
  })(api,daedalus);
store=(function(){
    "use strict";
    const globals={};
    return{globals};
  })();
audio=(function(api,daedalus){
    "use strict";
    const StyleSheet=daedalus.StyleSheet;
    const DomElement=daedalus.DomElement;
    const TextElement=daedalus.TextElement;
    const[AudioDevice]=(function(){
        let device_instance=null;
        class RemoteDeviceImpl{
          constructor(device){
            this.device=device;
            this.audio_instance=new Audio();
            this.auto_play=false;
            const bind=(x)=>{
              this.audio_instance['on'+x]=this['on'+x].bind(this);
            };
            bind('play');
            bind('loadstart');
            bind('playing');
            bind('pause');
            bind('durationchange');
            bind('timeupdate');
            bind('waiting');
            bind('stalled');
            bind('ended');
            bind('error');
          }
          setQueue(queue){
            const idList=queue.map(song=>song.id).filter(uuid=>!!uuid);
            api.queueSetQueue(idList).then(result=>{
                console.log(result);
              }).catch(result=>{
                console.log(result);
              });
            this.device._sendEvent('handleAudioQueueChanged',queue);
          }
          updateQueue(index,queue){

          }
          loadQueue(){
            return api.queueGetQueue();
          }
          createQueue(query){
            return api.queueCreate(query,50);
          }
          playSong(index,song){
            const url=api.librarySongAudioUrl(song.id);
            this.audio_instance.src=url;
            this.audio_instance.volume=.75;
            this.auto_play=true;
          }
          play(){
            this.audio_instance.play();
          }
          stop(){
            if(this.isPlaying()){
              this.pause();
            }
            this.device._sendEvent('handleAudioSongChanged',null);
          }
          pause(){
            if(this.isPlaying()){
              this.audio_instance.pause();
            }
          }
          currentTime(){
            return this.audio_instance.currentTime;
          }
          setCurrentTime(time){
            this.audio_instance.currentTime=time;
          }
          duration(){
            return this.audio_instance.duration;
          }
          setVolume(volume){
            this.audio_instance.volume=volume;
          }
          isPlaying(){
            return this.audio_instance&&this.audio_instance.currentTime>0&&!this.audio_instance.paused&&!this.audio_instance.ended&&this.audio_instance.readyState>2;
            
          }
          onloadstart(event){
            console.log('audio on load start');
            if(this.auto_play){
              this.audio_instance.play();
            }
            this.device._sendEvent('handleAudioLoadStart',{});
          }
          onplay(event){
            this.device._sendEvent('handleAudioPlay',{});
          }
          onplaying(event){
            console.log("playing",event);
            this.device._sendEvent('handleAudioPlay',{});
          }
          onpause(event){
            this.device._sendEvent('handleAudioPause',{});
          }
          onwaiting(event){
            this.device._sendEvent('handleAudioWaiting',{});
          }
          onstalled(event){
            this.device._sendEvent('handleAudioStalled',{});
          }
          ontimeupdate(event){
            this.device._sendEvent('handleAudioTimeUpdate',{currentTime:this.audio_instance.currentTime,
                              duration:this.audio_instance.duration});
          }
          ondurationchange(event){
            this.device._sendEvent('handleAudioDurationChange',{currentTime:this.audio_instance.currentTime,
                              duration:this.audio_instance.duration});
          }
          onended(event){
            console.log("on ended",this.current_index);
            this.device._sendEvent('handleAudioEnded',event);
            this.device.next();
          }
          onerror(event){
            console.log("on error",this.current_index);
            this.device._sendEvent('handleAudioError',event);
            this.device.next();
          }
        }
        function mapSongToObj(song){
          return{url:api.librarySongAudioUrl(song.id),artist:song.artist,album:song.album,
                      title:song.title,length:song.length,file_path:song.file_path,spk:song.spk,
                      id:song.id};
        }
        class NativeDeviceImpl{
          constructor(device){
            this.device=device;
            console.error("-------------------------------------");
            const bind=(x)=>{
              registerAndroidEvent('on'+x,this['on'+x].bind(this));
            };
            bind('prepared');
            bind('play');
            bind('pause');
            bind('stop');
            bind('error');
            bind('timeupdate');
            bind('indexchanged');
            this._currentTime=0;
            this._duration=0;
          }
          setQueue(queue){
            console.log("setting queue");
            return new Promise((accept,reject)=>{
                const lst=queue.map(mapSongToObj);
                const data=JSON.stringify(lst);
                AndroidNativeAudio.setQueue(data);
                this.device._sendEvent('handleAudioQueueChanged',queue);
                accept(true);
              });
          }
          updateQueue(index,queue){
            console.log("updating queue");
            return new Promise((accept,reject)=>{
                const lst=queue.map(mapSongToObj);
                const data=JSON.stringify(lst);
                AndroidNativeAudio.updateQueue(index,data);
                accept(true);
              });
          }
          loadQueue(){
            console.log("loading queue");
            return new Promise((accept,reject)=>{
                console.log("loading queue: from promise");
                let data;
                try{
                  data=AndroidNativeAudio.getQueue();
                }catch(e){
                  console.error("load queue error: "+e.message);
                };
                if(data.length>0){
                  let tracks=JSON.parse(data);
                  console.log("loading queue: "+tracks.length);
                  accept({result:tracks});
                }else{
                  console.log("loading queue: error");
                  accept({result:[]});
                }
              });
          }
          createQueue(query){
            return api.queueCreate(query,50);
          }
          playSong(index,song){
            AndroidNativeAudio.loadIndex(index);
          }
          play(){
            AndroidNativeAudio.play();
          }
          pause(){
            AndroidNativeAudio.pause();
          }
          stop(){
            AndroidNativeAudio.stop();
            this.device._sendEvent('handleAudioSongChanged',null);
          }
          currentTime(){
            return this._currentTime;
          }
          setCurrentTime(time){
            const pos=Math.floor(time*1000);
            console.log(`setting time to ${pos} / ${this._duration}`);
            AndroidNativeAudio.seekms(pos);
            return;
          }
          duration(){
            return this._duration;
          }
          setVolume(volume){
            return;
          }
          isPlaying(){
            return AndroidNativeAudio.isPlaying();
          }
          onprepared(payload){

          }
          onplay(payload){
            this.device._sendEvent('handleAudioPlay',{});
          }
          onpause(payload){
            this.device._sendEvent('handleAudioPause',{});
          }
          onstop(payload){
            this.device._sendEvent('handleAudioStop',{});
          }
          onerror(payload){

          }
          ontimeupdate(payload){
            if(payload.currentIndex!=this.device.current_index){
              console.error("detected out of date information");
              this.device.current_index=payload.currentIndex;
              if(payload.currentIndex>=0&&payload.currentIndex<this.device.queue.length){
              
                let song=this.device.queue[payload.currentIndex];
                this.device._sendEvent('handleAudioSongChanged',{...song,index:payload.currentIndex});
                
              }
            }
            this._currentTime=payload.position/1000;
            this._duration=payload.duration/1000;
            this.device._sendEvent('handleAudioTimeUpdate',{currentTime:this._currentTime,
                              duration:this._duration,currentIndex:this.device.current_index});
            
          }
          onindexchanged(payload){
            const index=payload.index;
            this.device.current_song=this.device.queue[index];
            this.device._sendEvent('handleAudioSongChanged',{...this.device.current_song,
                              index});
          }
        }
        class AudioDevice{
          constructor(){
            this.connected_elements=[];
            this.current_index=-1;
            this.current_song=null;
            this.queue=[];
            this.impl=null;
          }
          setImpl(impl){
            this.impl=impl;
          }
          queueGet(){
            return this.queue;
          }
          queueLength(){
            return this.queue.length;
          }
          queueSet(songList){
            this.queue=songList;
            this.impl.updateQueue(-1,this.queue);
            this.stop();
          }
          queueSave(){
            this.impl.setQueue(this.queue);
          }
          queueLoad(){
            this.impl.loadQueue().then(result=>{
                this.queue=result.result;
                this._sendEvent('handleAudioQueueChanged',this.queue);
              }).catch(error=>{
                console.log(error);
                this.queue=[];
                this.current_index=-1;
                this._sendEvent('handleAudioQueueChanged',this.queue);
              });
            this.stop();
          }
          queueCreate(query){
            this.impl.createQueue(query).then(result=>{
                this.queue=result.result;
                this.impl.updateQueue(this.current_index,this.queue);
                this._sendEvent('handleAudioQueueChanged',this.queue);
              }).catch(error=>{
                console.log(error);
                this.queue=[];
                this.current_index=-1;
                this.impl.updateQueue(this.current_index,this.queue);
                this._sendEvent('handleAudioQueueChanged',this.queue);
              });
            this.stop();
          }
          queueMoveSongUp(index){
            if(index>=1&&index<this.queue.length){
              const target=index-1;
              const a=this.queue.splice(index,1);
              this.queue.splice(target,0,a[0]);
              if(this.current_index==index){
                this.current_index=target;
              }else if(this.current_index==target){
                this.current_index+=1;
              }
              this.impl.updateQueue(this.current_index,this.queue);
              this._sendEvent('handleAudioQueueChanged',this.queue);
            }
          }
          queueMoveSongDown(index){
            if(index>=0&&index<this.queue.length-1){
              const target=index+1;
              const a=this.queue.splice(index,1);
              this.queue.splice(target,0,a[0]);
              if(this.current_index==index){
                this.current_index=target;
              }else if(this.current_index==target){
                this.current_index-=1;
              }
              this.impl.updateQueue(this.current_index,this.queue);
              this._sendEvent('handleAudioQueueChanged',this.queue);
            }
          }
          queueSwapSong(index,target){
            daedalus.util.array_move(this.queue,index,target);
            if(this.current_index==index){
              this.current_index=target;
            }else if(index<this.current_index&&target>=this.current_index){
              this.current_index-=1;
            }else if(index>this.current_index&&target<=this.current_index){
              this.current_index+=1;
            }
            this.impl.updateQueue(this.current_index,this.queue);
            this._sendEvent('handleAudioQueueChanged',this.queue);
          }
          queuePlayNext(song){
            const index=this.current_index+1;
            if(index>=0&&index<this.queue.length){
              this.queue.splice(index,0,song);
            }else if(index>=this.queue.length){
              this.queue.push(song);
            }else{
              this.current_index=-1;
              this.current_song=song;
              this.queue=[song];
              this._sendEvent('handleAudioSongChanged',null);
            }
            this.impl.updateQueue(this.current_index,this.queue);
            this._sendEvent('handleAudioQueueChanged',this.queue);
          }
          queueRemoveIndex(index){
            if(index>=0&&index<this.queue.length){
              this.queue.splice(index,1);
              console.log("queue, sliced",index,this.queue.length);
              if(this.current_index>=this.queue.length){
                this.pause();
                this.current_index=-1;
                this.current_song=null;
                this._sendEvent('handleAudioSongChanged',null);
              }else if(index==this.current_index){
                this.pause();
                this.current_song=this.queue[index];
                this._sendEvent('handleAudioSongChanged',{...this.queue[index],index});
                
              }else if(index<this.current_index){
                this.current_index-=1;
                this.current_song=this.queue[index];
              }
              console.log("queue, sliced update");
              this.impl.updateQueue(this.current_index,this.queue);
              this._sendEvent('handleAudioQueueChanged',this.queue);
            }
          }
          stop(){
            this.current_index=-1;
            this.current_song=null;
            this.impl.stop();
          }
          pause(){
            this.impl.pause();
          }
          _playSong(song){
            this.current_song=song;
            console.log(song);
            this.impl.playSong(this.current_index,this.current_song);
            this._sendEvent('handleAudioSongChanged',{...song,index:this.current_index});
            
          }
          playSong(song){
            this.current_index=-1;
            this._playSong(song);
          }
          playIndex(index){
            console.log(index);
            if(index>=0&&index<this.queue.length){
              this.current_index=index;
              this._playSong(this.queue[index]);
            }else{
              this.current_index=-1;
              this.current_song=null;
              this.stop();
              this._sendEvent('handleAudioSongChanged',null);
              console.warn("playIndex: invalid playlist index "+index);
            }
          }
          togglePlayPause(){
            if(this.impl.isPlaying()){
              this.impl.pause();
            }else{
              this.impl.play();
            }
          }
          next(){
            const idx=this.current_index+1;
            if(idx>=0&&idx<this.queue.length){
              this.playIndex(idx);
            }
          }
          prev(){
            const idx=this.current_index-1;
            if(idx>=0&&idx<this.queue.length){
              this.playIndex(idx);
            }
          }
          currentSongIndex(){
            return this.current_index;
          }
          currentSongId(){
            const idx=this.current_index;
            if(idx>=0&&idx<this.queue.length){
              return this.queue[idx].id;
            }
            return null;
          }
          currentSong(){
            const idx=this.current_index;
            if(idx>=0&&idx<this.queue.length){
              return this.queue[idx];
            }
            return null;
          }
          currentTime(){
            return this.impl.currentTime();
          }
          setCurrentTime(time){
            return this.impl.setCurrentTime(time);
          }
          duration(){
            return this.impl.duration();
          }
          setVolume(volume){
            return this.impl.setVolume(volume);
          }
          isPlaying(){
            return this.impl.isPlaying();
          }
          connectView(elem){
            if(this.connected_elements.filter(e=>e===elem).length>0){
              console.error("already connected view");
              return;
            }
            this.connected_elements.push(elem);
          }
          disconnectView(elem){
            this.connected_elements=this.connected_elements.filter(e=>e!==elem);
          }
          _sendEvent(eventname,event){
            this.connected_elements=this.connected_elements.filter(e=>e.isMounted(
                            ));
            this.connected_elements.forEach(e=>{
                if(e&&e[eventname]){
                  e[eventname](event);
                }
              });
          }
        }
        AudioDevice.instance=function(){
          if(device_instance===null){
            device_instance=new AudioDevice();
            let impl;
            if(daedalus.platform.isAndroid){
              impl=new NativeDeviceImpl(device_instance);
            }else{
              impl=new RemoteDeviceImpl(device_instance);
            }
            device_instance.setImpl(impl);
          }
          return device_instance;
        };
        return[AudioDevice];
      })();
    const[BrownNoiseContext,NoiseContext,OceanNoiseContext,PinkNoiseContext,WhiteNoiseContext]=(
          function(){
        const EasingFunctions={linear:t=>t,easeInQuad:t=>t*t,easeOutQuad:t=>t*(2-t),
                  easeInOutQuad:t=>t<.5?2*t*t:-1+(4-2*t)*t,easeInCubic:t=>t*t*t,easeOutCubic:t=>(
                      --t)*t*t+1,easeInOutCubic:t=>t<.5?4*t*t*t:(t-1)*(2*t-2)*(2*t-2)+1,easeInQuart:t=>t*t*t*t,
                  easeOutQuart:t=>1-(--t)*t*t*t,easeInOutQuart:t=>t<.5?8*t*t*t*t:1-8*(--t)*t*t*t,
                  easeInQuint:t=>t*t*t*t*t,easeOutQuint:t=>1+(--t)*t*t*t*t,easeInOutQuint:t=>t<.5?16*t*t*t*t*t:1+16*(
                      --t)*t*t*t*t};
        function mirror(f){
          return t=>{
            return t<.5?f(t*2.0):f(1.0-(t-.5)*2.0);
          };
        }
        class NoiseContext{
          constructor(color){
            this.ctxt=new(window.AudioContext||window.webkitAudioContext)();
            this.channels=2;
            this.duration=5;
            this.frameCount=this.duration*this.ctxt.sampleRate;
            this.color=color;
            this.buffer=this.ctxt.createBuffer(this.channels,this.frameCount,this.ctxt.sampleRate);
            
            this.fillBuffer(this.buffer);
            this.source=this.ctxt.createBufferSource();
            this.source.buffer=this.buffer;
            this.source.loop=true;
            this.gain=this.ctxt.createGain();
            this.source.connect(this.gain);
            this.gain.connect(this.ctxt.destination);
            this.gain.gain.value=.5;
            this.state="ready";
          }
          fillBuffer(buffer){
            if(mode=="white"){
              for(let chan_idx=0;chan_idx<buffer.numberOfChannels;chan_idx++)
              {
                let chan_data=buffer.getChannelData(chan_idx);
                for(let idx=0;idx<this.frameCount;idx++)
                {
                  chan_data[idx]=(Math.random()*2-1);
                }
              }
            }
          }
          play(){
            if(this.state==="ready"){
              this.source.start();
              this.state="playing";
            }else if(this.state==="paused"){
              this.ctxt.resume();
              this.state="playing";
            }
          }
          pause(){
            if(this.state==="playing"){
              this.ctxt.suspend();
              this.state="paused";
            }
          }
          stop(){
            if(this.state!=="stopped"){
              this.source.stop();
              this.state="stopped";
            }
          }
          setVolume(volume){
            this.gain.gain.value=volume;
          }
          getVolume(){
            return this.gain.gain.value;
          }
          isPlaying(){
            return this.state==="playing";
          }
        }
        class WhiteNoiseContext extends NoiseContext {
          constructor(){
            super("white");
          }
          fillBuffer(buffer){
            for(let chan_idx=0;chan_idx<buffer.numberOfChannels;chan_idx++)
            {
              let chan_data=buffer.getChannelData(chan_idx);
              for(let idx=0;idx<this.frameCount;idx++)
              {
                chan_data[idx]=(Math.random()*2-1);
              }
            }
          }
        }
        class PinkNoiseContext extends NoiseContext {
          constructor(){
            super("pink");
          }
          fillBuffer(buffer){
            for(let chan_idx=0;chan_idx<buffer.numberOfChannels;chan_idx++)
            {
              let chan_data=buffer.getChannelData(chan_idx);
              let b0=0.0,b1=0.0,b2=0.0,b3=0.0,b4=0.0,b5=0.0,b6=0.0;
              for(let idx=0;idx<this.frameCount;idx++)
              {
                let w=Math.random()*2-1;
                b0=0.99886*b0+w*0.0555179;
                b1=0.99332*b1+w*0.0750759;
                b2=0.96900*b2+w*0.1538520;
                b3=0.86650*b3+w*0.3104856;
                b4=0.55000*b4+w*0.5329522;
                b5=-0.7616*b5-w*0.0168980;
                chan_data[idx]=(b0+b1+b2+b3+b4+b5+b6+w*0.5362)/8.0;
                b6=w*0.115926;
              }
            }
          }
        }
        class BrownNoiseContext extends NoiseContext {
          constructor(){
            super("brown");
          }
          fillBuffer(buffer){
            const b=.01;
            const Fs=this.ctxt.sampleRate,Fc=1000;
            const a=Fs/(Fs+2*Math.PI*Fc);
            let f=mirror(EasingFunctions.easeInOutQuad);
            for(let chan_idx=0;chan_idx<buffer.numberOfChannels;chan_idx++)
            {
              let chan_data=buffer.getChannelData(chan_idx);
              let v=0;
              let p=0;
              for(let idx=0;idx<this.frameCount;idx++)
              {
                let w=(Math.random()*2-1);
                v=(v+(b*w))/(1+b);
                p=a*p+(1-a)*v;
                chan_data[idx]=p;
              }
              console.log(chan_data);
            }
          }
        }
        class OceanNoiseContext extends NoiseContext {
          constructor(){
            super("ocean");
          }
          fillBuffer(buffer){
            const b=.01;
            const Fs=this.ctxt.sampleRate,Fc=1000;
            const a=Fs/(Fs+2*Math.PI*Fc);
            let f=mirror(EasingFunctions.easeInOutQuad);
            for(let chan_idx=0;chan_idx<buffer.numberOfChannels;chan_idx++)
            {
              let chan_data=buffer.getChannelData(chan_idx);
              let v=0;
              let p=0;
              for(let idx=0;idx<this.frameCount;idx++)
              {
                let w=(Math.random()*2-1);
                v=(v+(b*w))/(1+b);
                let t=idx/this.frameCount;
                v=v*f(t);
                p=a*p+(1-a)*v;
                chan_data[idx]=p;
              }
              console.log(chan_data);
            }
          }
        }
        return[BrownNoiseContext,NoiseContext,OceanNoiseContext,PinkNoiseContext,
                  WhiteNoiseContext];
      })();
    const[]=(function(){
        return[];
      })();
    return{AudioDevice,BrownNoiseContext,NoiseContext,OceanNoiseContext,PinkNoiseContext,
          WhiteNoiseContext};
  })(api,daedalus);
pages=(function(api,audio,components,daedalus,resources,router,store){
    "use strict";
    const StyleSheet=daedalus.StyleSheet;
    const DomElement=daedalus.DomElement;
    const ButtonElement=daedalus.ButtonElement;
    const TextElement=daedalus.TextElement;
    const TextInputElement=daedalus.TextInputElement;
    const Router=daedalus.Router;
    const LinkElement=daedalus.LinkElement;
    const routes=router.routes;
    const[fmtEpochTime]=(function(){
        function fmtEpochTime(ms_time){
          const dt=new Date(ms_time);
          let d=dt.getDate();
          let m=dt.getMonth()+1;
          let y=dt.getFullYear();
          let H=dt.getHours();
          let M=dt.getMinutes();
          let S=dt.getSeconds();
          let Z=dt.getTimezoneOffset();
          let zh=-Math.floor(Z/60);
          let zm=Math.abs(Z)%60;
          if(zm<9){
            zm='0'+zm;
          }
          return`${y}/${m}/${d} ${H}:${M}:${S} ${zh}:${zm}`;
        }
        return[fmtEpochTime];
      })();
    const[LandingPage]=(function(){
        const styles={main:'dcs-0efdbccc-0',btn_center:'dcs-0efdbccc-1'};
        class LandingPage extends DomElement {
          constructor(){
            super("div",{className:styles.main},[]);
            this.attrs={btn:new ButtonElement("Login",()=>{
                  history.pushState({},"","/login");
                })};
            this.attrs.btn.updateProps({className:styles.btn_center});
            this.appendChild(this.attrs.btn);
            this.appendChild(new TextElement(daedalus.env.buildDate));
          }
        }
        return[LandingPage];
      })();
    const[LoginPage]=(function(){
        const style={main:'dcs-835de489-0',btn_center:'dcs-835de489-1',edit:'dcs-835de489-2',
                  warning:'dcs-835de489-3',hide:'dcs-835de489-4'};
        class LoginPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={btn1:new ButtonElement("Login",this.handleLoginClicked.bind(
                                  this)),btn2:new ButtonElement("Cancel",()=>{
                  history.pushState({},"","/");
                }),edit_username:new TextInputElement(""),edit_password:new TextInputElement(
                              ""),warning:new DomElement("div",{className:style.warning},[new TextElement(
                                      "Invalid Username or Password")])};
            this.attrs.btn1.addClassName(style.btn_center);
            this.attrs.btn2.addClassName(style.btn_center);
            this.attrs.edit_username.addClassName(style.edit);
            this.attrs.edit_password.addClassName(style.edit);
            this.attrs.edit_username.updateProps({placeholder:'Username'});
            this.attrs.edit_password.updateProps({placeholder:'Password',type:'password'});
            
            this.attrs.warning.addClassName(style.hide);
            this.appendChild(this.attrs.edit_username);
            this.appendChild(this.attrs.edit_password);
            this.appendChild(this.attrs.warning);
            this.appendChild(this.attrs.btn1);
            this.appendChild(this.attrs.btn2);
          }
          handleLoginClicked(){
            const username=this.attrs.edit_username.props.value;
            const password=this.attrs.edit_password.props.value;
            api.authenticate(username,password).then((data)=>{
                if(data.token){
                  api.setUsertoken(data.token);
                  this.attrs.warning.addClassName(style.hide);
                  history.pushState({},"",routes.userLibraryList());
                }else{
                  this.attrs.warning.removeClassName(style.hide);
                  console.error(data.error);
                }
              }).catch((err)=>{
                this.attrs.warning.removeClassName(style.hide);
                console.error(err);
              });
          }
        }
        return[LoginPage];
      })();
    const[FileSystemPage,PublicFilePage,StoragePage,StoragePreviewPage]=(function(
            ){
        const thumbnailFormats={jpg:true,png:true,webm:true,mp4:true,gif:true};
        const style={item_file:'dcs-8547c91b-0',list:'dcs-8547c91b-1',listItem:'dcs-8547c91b-2',
                  listItemMain:'dcs-8547c91b-3',listItemDir:'dcs-8547c91b-4',listItemMid:'dcs-8547c91b-5',
                  listItemEnd:'dcs-8547c91b-6',listItemText:'dcs-8547c91b-7',icon1:'dcs-8547c91b-8',
                  icon2:'dcs-8547c91b-9',fileDetailsShow:'dcs-8547c91b-10',fileDetailsHide:'dcs-8547c91b-11',
                  encryption:{"system":'dcs-8547c91b-12',"server":'dcs-8547c91b-13',"client":'dcs-8547c91b-14',
                      "none":'dcs-8547c91b-15'},svgDiv:'dcs-8547c91b-16',text:'dcs-8547c91b-17',
                  textSpacer:'dcs-8547c91b-18',callbackLink:'dcs-8547c91b-19',center:'dcs-8547c91b-20',
                  paddedText:'dcs-8547c91b-21',navBar:'dcs-8547c91b-22',searchShow:'dcs-8547c91b-23',
                  searchHide:'dcs-8547c91b-24',grow:'dcs-8547c91b-25',objectContainer:'dcs-8547c91b-26',
                  zoomOut:'dcs-8547c91b-27',zoomIn:'dcs-8547c91b-28',maxWidth:'dcs-8547c91b-29',
                  main2:'dcs-8547c91b-30',show:'dcs-8547c91b-31',hide:'dcs-8547c91b-32'};
        
        ;
        ;
        let thumbnail_work_queue=[];
        let thumbnail_work_count=0;
        function thumbnail_DoProcessNext(){
          if(thumbnail_work_queue.length>0){
            const elem=thumbnail_work_queue.shift();
            if(elem.props.src!=elem.state.url1){
              elem.updateProps({src:elem.state.url1});
            }else{
              thumbnail_ProcessNext();
            }
          }
        }
        function thumbnail_ProcessNext(){
          if(thumbnail_work_queue.length>0){
            requestIdleCallback(thumbnail_DoProcessNext);
          }else{
            thumbnail_work_count-=1;
          }
        }
        function thumbnail_ProcessStart(){
          if(thumbnail_work_queue.length>=3){
            requestIdleCallback(thumbnail_DoProcessNext);
            requestIdleCallback(thumbnail_DoProcessNext);
            requestIdleCallback(thumbnail_DoProcessNext);
            thumbnail_work_count=3;
          }else if(thumbnail_work_queue.length>0){
            requestIdleCallback(thumbnail_DoProcessNext);
            thumbnail_work_count=1;
          }
        }
        function thumbnail_CancelQueue(){
          thumbnail_work_queue=[];
          thumbnail_work_count=0;
        }
        class SvgIconElementImpl extends DomElement {
          constructor(url1,url2,props){
            super("img",{src:url2,width:80,height:60,...props},[]);
            this.state={url1,url2};
            if(url1!==url2&&url1&&url2){
              thumbnail_work_queue.push(this);
            }
          }
          onLoad(event){
            if(this.props.src===this.state.url2){
              return;
            }
            thumbnail_ProcessNext();
          }
          onError(error){
            console.warn("error loading: ",this.props.src,JSON.stringify(error));
            
            if(this.props.src===this.state.url2){
              return;
            }
            if(this.props.src!=this.state.url2&&this.state.url2){
              this.updateProps({src:this.state.url2});
            }
            thumbnail_ProcessNext();
          }
        }
        class SvgIconElement extends DomElement {
          constructor(url1,url2,props){
            if(url2===null){
              url2=url1;
            }
            super("div",{className:style.svgDiv},[new SvgIconElementImpl(url1,url2,
                                  props)]);
          }
        }
        class SvgMoreElement extends components.SvgElement {
          constructor(callback){
            super(resources.svg.more,{width:20,height:60,className:style.listItemEnd});
            
            this.state={callback};
          }
          onClick(event){
            this.state.callback();
          }
        }
        class StorageListElement extends DomElement {
          constructor(elem){
            super("div",{className:style.list},[]);
          }
        }
        class CallbackLink extends DomElement {
          constructor(text,callback){
            super('div',{className:style.callbackLink},[new TextElement(text)]);
            this.state={callback};
          }
          setText(text){
            this.children[0].setText(text);
          }
          onClick(){
            this.state.callback();
          }
        }
        class DirectoryElement extends DomElement {
          constructor(name,url){
            super("div",{className:style.listItemDir},[]);
            this.appendChild(new DomElement("div",{className:style.encryption["none"]},
                              []));
            this.appendChild(new SvgIconElement(resources.svg.folder,null,{className:style.icon1}));
            
            this.appendChild(new components.MiddleTextLink(name,url));
            this.children[2].addClassName(style.listItemMid);
          }
        }
        class DownloadLink extends DomElement {
          constructor(url,filename){
            super("a",{href:url,download:filename},[new TextElement("Download")]);
            
          }
        }
        class FileElement extends DomElement {
          constructor(fileInfo,callback,delete_callback){
            super("div",{className:style.listItem},[]);
            this.state={fileInfo};
            this.attrs={main:this.appendChild(new DomElement("div",{className:style.listItemMain},
                                  [])),details:null,delete_callback};
            const elem=new components.MiddleText(fileInfo.name);
            elem.addClassName(style.listItemMid);
            elem.updateProps({onClick:this.handleShowDetails.bind(this)});
            let url1=resources.svg.file;
            let url2=null;
            let className=style.icon1;
            const ext=daedalus.util.splitext(fileInfo.name)[1].substring(1).toLocaleLowerCase(
                        );
            if(thumbnailFormats[ext]===true){
              url2=url1;
              url1=api.fsPathPreviewUrl(fileInfo.root,daedalus.util.joinpath(fileInfo.path,
                                  fileInfo.name));
              className=style.icon2;
            }
            const encryption=fileInfo.encryption||"none";
            this.attrs.main.appendChild(new DomElement("div",{className:style.encryption[
                                    encryption]},[]));
            this.attrs.main.appendChild(new SvgIconElement(url1,url2,{className:className}));
            
            this.attrs.main.appendChild(elem);
            if(callback!==null){
              this.attrs.main.appendChild(new SvgMoreElement(callback));
            }
          }
          handleShowDetails(event){
            if(this.attrs.details===null){
              const fpath=daedalus.util.joinpath(this.state.fileInfo.path,this.state.fileInfo.name);
              
              this.attrs.details=new DomElement("div",{className:style.fileDetailsShow},
                              []);
              this.appendChild(this.attrs.details);
              this.attrs.details.appendChild(new DomElement('div',{className:style.paddedText},
                                  [new LinkElement("Preview",api.fsGetPathContentUrl(this.state.fileInfo.root,
                                              fpath))]));
              const dl=new DownloadLink(api.fsPathUrl(this.state.fileInfo.root,fpath,
                                  1),this.state.fileInfo.name);
              this.attrs.details.appendChild(new DomElement('div',{className:style.paddedText},
                                  [dl]));
              this.attrs.details.appendChild(new DomElement('div',{className:style.paddedText},
                                  [new CallbackLink("Delete",this.attrs.delete_callback)]));
              if(this.state.fileInfo.encryption=="system"){
                this.attrs.public_container=new DomElement('div',{className:style.paddedText},
                                  []);
                this.attrs.public_link1=new CallbackLink("Generate Public Link",this.handlePublic1Clicked.bind(
                                      this));
                this.attrs.public_link2=new CallbackLink("Open Public Download Page",
                                  this.handlePublic2Clicked.bind(this));
                this.attrs.public_container.appendChild(this.attrs.public_link1);
                
                this.attrs.public_container.appendChild(this.attrs.public_link2);
                
                this.attrs.details.appendChild(this.attrs.public_container);
                this._updatePublicLinkText();
              }
              this.attrs.details.appendChild(new DomElement('div',{},[new TextElement(
                                          `Version: ${this.state.fileInfo.version}`)]));
              this.attrs.details.appendChild(new DomElement('div',{},[new TextElement(
                                          `Size: ${this.state.fileInfo.size}`)]));
              this.attrs.details.appendChild(new DomElement('div',{},[new TextElement(
                                          `Encryption: ${this.state.fileInfo.encryption}`)]));
              const dt=new Date(this.state.fileInfo.mtime*1000);
              this.attrs.details.appendChild(new DomElement('div',{},[new TextElement(
                                          `Modified Time: ${dt}`)]));
            }else{
              if(this.attrs.details.props.className===style.fileDetailsShow){
                this.attrs.details.updateProps({className:style.fileDetailsHide});
                
              }else{
                this.attrs.details.updateProps({className:style.fileDetailsShow});
                
              }
            }
          }
          handlePublic1Clicked(){
            console.log("click");
            console.log(this.state.fileInfo);
            const root=this.state.fileInfo.root;
            const name=this.state.fileInfo.path+"/"+this.state.fileInfo.name;
            if(this.state.fileInfo.public){
              api.fsPublicUriRevoke(root,name).then(result=>{
                  this.state.fileInfo.public=null;
                  this._updatePublicLinkText();
                }).catch(error=>{
                  console.error(error);
                });
            }else{
              api.fsPublicUriGenerate(root,name).then(result=>{
                  console.log("***",result);
                  this.state.fileInfo.public=result.result['id'];
                  this._updatePublicLinkText();
                }).catch(error=>{
                  console.error(error);
                });
            }
          }
          handlePublic2Clicked(){
            console.log("click");
            console.log(this.state.fileInfo);
            const uid=this.state.fileInfo.public;
            const filename=this.state.fileInfo.name;
            let url=location.origin+router.routes.publicFile({uid,filename});
            window.open(url,'_blank');
          }
          _updatePublicLinkText(){
            console.log(this.state.fileInfo.public);
            let text=this.state.fileInfo.public?"Revoke Public Link":"Generate Public Link";
            
            this.attrs.public_link1.setText(text);
            if(this.state.fileInfo.public){
              this.attrs.public_link2.removeClassName(style.hide);
              this.attrs.public_link2.addClassName(style.show);
            }else{
              this.attrs.public_link2.removeClassName(style.show);
              this.attrs.public_link2.addClassName(style.hide);
            }
          }
        }
        class StorageNavBar extends DomElement {
          constructor(){
            super("div",{className:style.navBar},[]);
          }
          addActionElement(element){
            this.appendChild(element);
          }
        }
        class Header extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['return'],parent.handleOpenParent.bind(parent));
            
            this.addAction(resources.svg['upload'],this.handleUploadFile.bind(this));
            
            this.addAction(resources.svg['search_generic'],parent.handleToggleSearch.bind(
                              parent));
            this.addAction(resources.svg['new_folder'],parent.handleNewDirectory.bind(
                              parent));
            this.attrs.location=new components.MiddleText(".....");
            this.addRow(true);
            this.addRowElement(0,this.attrs.location);
            this.attrs.search_input=new TextInputElement("",null,parent.search.bind(
                              parent));
            this.addRow(false);
            this.attrs.search_input.addClassName(style.grow);
            this.addRowElement(1,this.attrs.search_input);
            this.addRowAction(1,resources.svg['search_generic'],()=>{});
            this.attrs.uploadManager=new StorageUploadManager(parent.handleInsertUploadFile.bind(
                              parent));
            this.addRow(true);
            this.addRowElement(2,this.attrs.uploadManager);
          }
          setLocation(path){
            this.attrs.location.setText(path);
          }
          setSearchText(text){
            this.attrs.search_input.setText(text);
          }
          handleUploadFile(){
            if(this.attrs.parent.state.match.root!==""){
              this.attrs.uploadManager.startUpload(this.attrs.parent.state.match.root,
                              this.attrs.parent.state.match.dirpath);
            }
          }
        }
        class StorageUploadManager extends StorageListElement {
          constructor(insert_callback){
            super();
            this.attrs={files:{},root:null,dirpath:null,insert_callback};
          }
          startUpload(root,dirpath){
            api.fsUploadFile(root,dirpath,{},{crypt:'system'},this.handleUploadFileSuccess.bind(
                              this),this.handleUploadFileFailure.bind(this),this.handleUploadFileProgress.bind(
                              this));
            this.attrs.root=root;
            this.attrs.dirpath=dirpath;
          }
          handleUploadFileSuccess(msg){
            const item=this.attrs.files[msg.fileName];
            item.fileInfo.mtime=msg.lastModified;
            this.attrs.insert_callback(item.fileInfo);
            setTimeout(()=>this.handleRemove(msg),1000);
          }
          handleUploadFileFailure(msg){
            console.error(msg);
            setTimeout(()=>this.handleRemove(msg),3000);
          }
          handleUploadFileProgress(msg){
            if(msg.first){
              const fileInfo={encryption:'system',mtime:0,name:msg.fileName,path:this.attrs.root,
                              permission:0,public:"",root:this.attrs.dirpath,size:msg.fileSize,
                              version:1,bytesTransfered:msg.bytesTransfered};
              const node=new TextElement(msg.fileName);
              this.attrs.files[msg.fileName]={fileInfo,node};
              this.appendChild(node);
            }else if(msg.finished){
              const item=this.attrs.files[msg.fileName];
              if(msg.bytesTransfered==msg.fileSize){
                item.node.setText(`${msg.fileName}: upload success`);
              }else{
                item.node.setText(`${msg.fileName}: upload failed`);
              }
            }else{
              const item=this.attrs.files[msg.fileName];
              item.fileInfo.bytesTransfered=msg.bytesTransfered;
              item.node.setText(`${msg.fileName} ${(100.0*msg.bytesTransfered/msg.fileSize).toFixed(
                                  2)}%`);
            }
          }
          handleRemove(msg){
            const item=this.attrs.files[msg.fileName];
            this.removeChild(item.node);
            delete this.attrs.files[msg.fileName];
          }
        }
        class StoragePage extends DomElement {
          constructor(){
            super("div",{},[]);
            this.attrs={header:new Header(this),regex:daedalus.patternToRegexp(":root?/:dirpath*",
                              false),lst:new StorageListElement(),more:new components.MoreMenu(
                              this.handleHideFileMore.bind(this)),filemap:{}};
            this.state={parent_url:null};
            this.appendChild(this.attrs.more);
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.lst);
          }
          elementMounted(){
            const params=daedalus.util.parseParameters();
            this.attrs.header.setSearchText((params.q&&params.q[0])||"");
          }
          elementUpdateState(oldState,newState){
            if(newState.match){
              if(!oldState.match||oldState.match.path!==newState.match.path){
                const match=daedalus.locationMatch(this.attrs.regex,newState.match.path);
                
                Object.assign(newState.match,match);
                this.handleRouteChange(newState.match.root,newState.match.dirpath);
                
              }
            }else{
              newState.match={root:"",dirpath:""};
            }
          }
          getRoots(){
            thumbnail_CancelQueue();
            this.attrs.lst.removeChildren();
            api.fsGetRoots().then(data=>{
                this.handleGetRoots(data.result);
              }).catch(error=>{
                this.handleGetRootsError(error);
              });
          }
          handleGetRoots(result){
            console.log(result);
            this.updateState({parent_url:null});
            result.forEach(name=>{
                let url='/u/storage/list/'+this.state.match.path+'/'+name;
                url=url.replace(/\/\//,'/');
                this.attrs.lst.appendChild(new DirectoryElement(name,url));
              });
          }
          handleGetRootsError(error){
            console.error(error);
            this.updateState({parent_url:null});
            this.attrs.lst.appendChild(new TextElement("error loading roots"));
          }
          refresh(){
            this.getPath(this.state.match.root,this.state.match.dirpath);
          }
          getPath(root,dirpath){
            thumbnail_CancelQueue();
            this.attrs.lst.removeChildren();
            api.fsGetPath(root,dirpath).then(data=>{
                this.handleGetPath(data.result);
              }).catch(error=>{
                this.handleGetPathError(error);
              });
          }
          handleGetPath(result){
            if(result===undefined){
              this.attrs.lst.appendChild(new TextElement("Empty Directory (error)"));
              
              return;
            }
            let url;
            if(result.parent===result.path){
              url=daedalus.util.joinpath('/u/storage/list/');
            }else{
              url=daedalus.util.joinpath('/u/storage/list/',result.name,result.parent);
              
            }
            this.updateState({parent_url:url});
            const filemap={};
            if((result.files.length+result.directories.length)===0){
              this.attrs.lst.appendChild(new TextElement("Empty Directory"));
            }else{
              result.directories.forEach(name=>{
                  let url=daedalus.util.joinpath('/u/storage/list/',this.state.match.path,
                                      name);
                  this.attrs.lst.appendChild(new DirectoryElement(name,url));
                });
              result.files.forEach(item=>{
                  const cbk=()=>{
                    this.handleShowFileMore(item);
                  };
                  item.root=this.state.match.root;
                  item.path=this.state.match.dirpath;
                  const elem=new FileElement(item,cbk,this.deleteElement.bind(this));
                  
                  this.attrs.lst.appendChild(elem);
                  filemap[item.name]=elem;
                });
            }
            thumbnail_ProcessStart();
            this.attrs.filemap=filemap;
          }
          handleGetPathError(error){
            let url='/u/storage/list/';
            console.log(error);
            if(this.state.match&&this.state.match.dirpath){
              const parts=daedalus.util.splitpath(this.state.match.dirpath);
              parts.pop();
              url=daedalus.util.joinpath(url,this.state.match.root,...parts);
            }
            this.updateState({parent_url:url});
            this.attrs.lst.appendChild(new TextElement("Empty Directory (error)"));
            
          }
          handleToggleSearch(){
            if(this.state.match.root===""){
              return;
            }
            if(this.attrs.search_input.props.className[0]==style.searchHide){
              this.attrs.search_input.updateProps({className:[style.searchShow]});
              
              thumbnail_CancelQueue();
              this.attrs.lst.removeChildren();
            }else{
              this.attrs.search_input.updateProps({className:[style.searchHide]});
              
              this.refresh();
            }
          }
          search(text){
            console.log(text);
            thumbnail_CancelQueue();
            this.attrs.lst.removeChildren();
            let root=this.state.match.root,path=this.state.match.dirpath,terms=text,
                          page=0,limit=100;
            api.fsSearch(root,path,terms,page,limit).then(this.handleSearchResult.bind(
                              this)).catch(this.handleSearchError.bind(this));
          }
          handleSearchResult(result){
            const files=result.result.files;
            console.log(files);
            const filemap={};
            if(files.length===0){
              this.attrs.lst.appendChild(new TextElement("No Results"));
            }else{
              try{
                files.forEach(item=>{
                    const cbk1=()=>{
                      this.handleShowFileMore(item);
                    };
                    const cbk2=this.deleteElement.bind(this);
                    item.root=this.state.match.root;
                    const parts=daedalus.util.splitpath(item.file_path);
                    parts.pop();
                    item.path=parts.join("/");
                    console.log(item);
                    const elem=new FileElement(item,cbk1,cbk2);
                    this.attrs.lst.appendChild(elem);
                    filemap[item.name]=elem;
                  });
              }catch(e){
                console.log(e);
              };
            }
            this.attrs.filemap=filemap;
            thumbnail_ProcessStart();
          }
          handleSearchError(error){
            this.attrs.lst.appendChild(new TextElement("No Results"));
          }
          handleOpenParent(){
            if(this.state.parent_url){
              history.pushState({},"",this.state.parent_url);
            }
          }
          handleShowFileMore(item){
            this.attrs.more.show();
          }
          handleHideFileMore(){
            this.attrs.more.hide();
          }
          handleRouteChange(root,dirpath){
            if(root===""&&dirpath===""){
              this.getRoots();
            }else if(root!==""){
              this.getPath(root,dirpath);
            }
            this.attrs.header.setLocation(root+"/"+dirpath);
          }
          handleInsertUploadFile(fileInfo){
            if(this.attrs.filemap[fileInfo.name]===undefined){
              const elem=new FileElement(fileInfo,null,this.deleteElement.bind(this));
              
              this.attrs.lst.insertChild(0,elem);
            }else{
              const elem=filemap[fileInfo.name];
              this.attrs.lst.insertChild(0,elem);
            }
          }
          deleteElement(elem,fileInfo){
            console.log(fileInfo);
          }
          handleNewDirectory(){
            console.log("mkdir");
          }
        }
        class FormattedText extends DomElement {
          constructor(text){
            super("pre",{style:{margin:0}},[new TextElement(text)]);
          }
          setText(text){
            this.children[0].setText(text);
          }
        }
        const preview_formats={'.mp4':'video','.webm':'video','.mkv':'video','.jpg':'image',
                  '.jpeg':'image','.gif':'image','.png':'image','.bmp':'image','.wav':'audio',
                  '.mp3':'audio','.ogg':'audio','.pdf':'pdf'};
        class StoragePreviewPage extends DomElement {
          constructor(){
            super("div",{},[]);
            this.attrs={regex:daedalus.patternToRegexp(":root?/:dirpath*",false)};
            
            console.log(this.attrs.regex);
          }
          elementUpdateState(oldState,newState){
            if(newState.match&&(!oldState.match||oldState.match.path!==newState.match.path)){
            
              const match=daedalus.locationMatch(this.attrs.regex,newState.match.path);
              
              Object.assign(newState.match,match);
              this.handleRouteChange(newState.match.root,newState.match.dirpath);
              
            }else{
              if(newState.match){
                Object.assign(newState.match,{root:"",path:""});
              }else{
                newState.match={root:"",path:""};
              }
            }
          }
          handleRouteChange(root,path){
            const[_,ext]=daedalus.util.splitext(path.toLocaleLowerCase());
            const format=preview_formats[ext];
            console.log(`display content ext: ${ext} format: ${format}`);
            if(format===undefined){
              api.fsGetPathContent(root,path).then(res=>{
                  this.appendChild(new FormattedText(res));
                }).catch(err=>console.error(err));
            }else if(format==='image'){
              const url=api.fsPathUrl(root,path,0);
              console.log(url);
              let img=new DomElement("img",{src:url,className:style.maxWidth},[]);
              
              let div=new DomElement("div",{className:style.zoomIn,onClick:this.toggleImageZoom.bind(
                                      this)},[img]);
              this.attrs.img=img;
              this.attrs.img_div=div;
              this.appendChild(div);
            }else if(format==='video'){
              const url=api.fsPathUrl(root,path,0);
              this.appendChild(new DomElement("video",{src:url,controls:1},[]));
            }else if(format==='audio'){
              const url=api.fsPathUrl(root,path,0);
              this.appendChild(new DomElement("audio",{src:url,controls:1},[]));
            }else if(format==='pdf'){
              const url=api.fsPathUrl(root,path,0);
              console.warn(url);
              this.addClassName(style.objectContainer);
              this.appendChild(new DomElement("object",{data:url,type:'application/pdf',
                                      width:'100%',height:'100%'},[]));
            }
          }
          toggleImageZoom(event){
            if(this.attrs.img_div.hasClassName(style.zoomIn)){
              this.attrs.img_div.removeClassName(style.zoomIn);
              this.attrs.img.removeClassName(style.maxWidth);
              this.attrs.img_div.addClassName(style.zoomOut);
            }else{
              this.attrs.img_div.removeClassName(style.zoomOut);
              this.attrs.img_div.addClassName(style.zoomIn);
              this.attrs.img.addClassName(style.maxWidth);
            }
          }
        }
        class FileSystemDirectoryElement extends DomElement {
          constructor(parent,name,url){
            super("div",{className:style.listItemDir},[]);
            this.appendChild(new DomElement("div",{className:style.encryption["none"]},
                              []));
            this.appendChild(new SvgIconElement(resources.svg.folder,null,{className:style.icon1}));
            
            this.attrs.text=this.appendChild(new components.MiddleText(name));
            this.attrs.text.props.onClick=this.handleClick.bind(this);
            this.children[2].addClassName(style.listItemMid);
            this.attrs.parent=parent;
            this.attrs.url=url;
          }
          handleClick(){
            this.attrs.parent.setCurrentPath(this.attrs.url);
          }
        }
        class FileSystemFileElement extends DomElement {
          constructor(parent,item,url){
            super("div",{className:style.listItemDir},[]);
            this.appendChild(new DomElement("div",{className:style.encryption["none"]},
                              []));
            this.appendChild(new SvgIconElement(resources.svg.file,null,{className:style.icon1}));
            
            this.appendChild(new components.MiddleText(item.name));
            this.children[2].addClassName(style.listItemMid);
            this.attrs.parent=parent;
            this.attrs.url=url;
          }
        }
        class FileSystemHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['return'],parent.handleOpenParent.bind(parent));
            
            this.attrs.location=new components.MiddleText(".....");
            this.addRow(true);
            this.addRowElement(0,this.attrs.location);
          }
          setLocation(location){
            this.attrs.location.setText(location);
          }
        }
        class FileSystemPage extends DomElement {
          constructor(){
            super("div",{},[]);
            this.attrs={header:new FileSystemHeader(this),lst:new StorageListElement(
                            ),current_path:"/"};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.lst);
          }
          elementMounted(){
            this.setCurrentPath("/");
          }
          handleOpenParent(){
            this.setCurrentPath(daedalus.util.dirname(this.attrs.current_path));
          }
          setCurrentPath(path){
            if(path.length===0){
              path="/";
            }
            console.log(`navigate to \`${path}\``);
            this.attrs.current_path=path;
            this.attrs.lst.removeChildren();
            this.attrs.header.setLocation(path);
            if(daedalus.platform.isAndroid){
              let result=Client.listDirectory(path);
              result=JSON.parse(result);
              this.updateContents(result);
            }else{
              const result={files:[{name:"file1"}],directories:["dir0","dir1"]};
              this.updateContents(result);
            }
            return;
          }
          updateContents(result){
            if((result.files.length+result.directories.length)===0){
              this.attrs.lst.appendChild(new TextElement("Empty Directory"));
            }else{
              result.directories.forEach(name=>{
                  let url=daedalus.util.joinpath(this.attrs.current_path,name);
                  const elem=new FileSystemDirectoryElement(this,name,url);
                  this.attrs.lst.appendChild(elem);
                });
              result.files.forEach(item=>{
                  let url=daedalus.util.joinpath(this.attrs.current_path,name);
                  const elem=new FileSystemFileElement(this,item,url);
                  this.attrs.lst.appendChild(elem);
                });
            }
          }
        }
        class PublicFilePage extends DomElement {
          constructor(){
            super("div",{className:style.main2},[]);
          }
          elementMounted(){
            const m=this.state.match;
            const url=api.fsGetPublicPathUrl(m.uid,m.filename);
            api.fsPublicUriInfo(m.uid,m.filename).then(result=>{
                this.appendChild(new DomElement("h2",{},[new TextElement("Download File")]));
                
                this.appendChild(new DomElement("a",{href:url,download:m.filename},
                                      [new TextElement(m.filename)]));
                this.appendChild(new components.VSpacer("1em"));
                this.appendChild(new TextElement("File Size: "+Math.floor(result.result.file.size/1024)+"kb"));
                
              }).catch(error=>{
                this.appendChild(new DomElement("h2",{},[new TextElement("File Not Found")]));
                
              });
          }
        }
        return[FileSystemPage,PublicFilePage,StoragePage,StoragePreviewPage];
      })();
    const[NoteContentPage,NoteContext,NoteEditPage,NotesPage]=(function(){
        const styles={page:'dcs-f1fbc9ce-0',list:'dcs-f1fbc9ce-1',item:'dcs-f1fbc9ce-2',
                  padding1:'dcs-f1fbc9ce-3',padding2:'dcs-f1fbc9ce-4',contentDiv:'dcs-f1fbc9ce-5',
                  contentPre:'dcs-f1fbc9ce-6',contentText:'dcs-f1fbc9ce-7'};
        class NoteContext{
          constructor(root,base){
            this.root=root;
            this.base=base;
            this.cache={};
          }
          getList(){
            return api.fsNoteList(this.root,this.base);
          }
          getContent(noteId){
            if(this.cache[noteId]!==undefined){
              return new Promise((resolve,reject)=>{
                  resolve(this.cache[noteId]);
                });
            }else{
              return new Promise((resolve,reject)=>{
                  api.fsNoteGetContent(this.root,this.base,noteId,null,null).then(
                                      result=>{
                      this.cache[noteId]=result;
                      resolve(result);
                    }).catch(error=>{
                      reject(error);
                    });
                });
            }
          }
          setContent(noteId,content){
            this.cache[noteId]=content;
            return api.fsNoteSetContent(this.root,this.base,noteId,content,null,null);
            
          }
        }
        function initContext(){
          if(store.globals.note_ctxt===undefined){
            store.globals.note_ctxt=new NoteContext("default","public/notes");
          }
          return store.globals.note_ctxt;
        }
        class ListHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
          }
        }
        class ListFooter extends components.NavFooter {
          constructor(parent){
            super();
            this.attrs.parent=parent;
          }
        }
        class NotesItem extends DomElement {
          constructor(ctxt,note_id,info){
            super("div",{className:styles.item},[]);
            this.attrs={ctxt,note_id,info};
            this.appendChild(new DomElement("div",{},[new TextElement(info.title)]));
            
            const t=fmtEpochTime(info.mtime*1000);
            this.appendChild(new DomElement("div",{},[new TextElement(`Modified Time: ${t}`)]));
            
          }
          onClick(event){
            router.navigate(router.routes.userNotesContent({noteId:this.attrs.note_id},
                              {}));
          }
        }
        class NotesList extends DomElement {
          constructor(parent,index,song){
            super("div",{className:styles.list},[]);
          }
          setNotes(ctxt,notes){
            this.removeChildren();
            const sorted_keys=Object.keys(notes).sort();
            console.log(sorted_keys);
            for(let note_id of sorted_keys){
              console.log(note_id,notes[note_id]);
              let info=notes[note_id];
              this.appendChild(new NotesItem(ctxt,note_id,info));
            }
          }
        }
        class NotesPage extends DomElement {
          constructor(){
            super("div",{className:styles.page},[]);
            this.attrs={header:new ListHeader(this),footer:new ListFooter(this),container:new NotesList(
                            ),padding1:new DomElement("div",{className:styles.padding1},[]),padding2:new DomElement(
                              "div",{className:styles.padding2},[])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.padding1);
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.padding2);
            this.appendChild(this.attrs.footer);
            this.attrs.ctxt=initContext();
          }
          elementMounted(){
            console.log("mount library view");
            this.attrs.ctxt.getList().then(result=>{
                console.log(result.result);
                this.attrs.container.setNotes(this.attrs.ctxt,result.result);
              }).catch(error=>{
                console.log(error);
              });
          }
        }
        class ContentHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['return'],()=>{
                router.navigate(router.routes.userNotesList({},{}));
              });
            this.addAction(resources.svg['edit'],()=>{
                router.navigate(router.routes.userNotesEdit({noteId:this.attrs.parent.state.match.noteId},
                                      {}));
              });
          }
        }
        class ContentFooter extends components.NavFooter {
          constructor(parent){
            super();
            this.attrs.parent=parent;
          }
        }
        class NoteContentPage extends DomElement {
          constructor(){
            super("div",{className:styles.page},[]);
            this.attrs={header:new ContentHeader(this),footer:new ContentFooter(this),
                          container:new DomElement("div",{className:styles.contentDiv},[]),padding1:new DomElement(
                              "div",{className:styles.padding1},[]),padding2:new DomElement("div",
                              {className:styles.padding2},[])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.padding1);
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.padding2);
            this.appendChild(this.attrs.footer);
            this.attrs.ctxt=initContext();
          }
          elementMounted(){
            this.showContent();
          }
          showContent(){
            this.attrs.container.removeChildren();
            this.attrs.ctxt.getContent(this.state.match.noteId).then(result=>{
                this.attrs.container.appendChild(new DomElement("pre",{className:styles.contentPre},
                                      [new TextElement(result+"\n\n\n")]));
              }).catch(error=>{
                console.log(error);
              });
          }
          beginEdit(){
            this.attrs.container.removeChildren();
            this.attrs.ctxt.getContent(this.state.match.noteId).then(result=>{
                this.attrs.container.appendChild(new DomElement("textarea",{className:styles.contentText},
                                      [new TextElement(result)]));
              }).catch(error=>{
                console.log(error);
              });
          }
        }
        class EditHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['discard'],()=>{
                router.navigate(router.routes.userNotesContent({noteId:this.attrs.parent.state.match.noteId},
                                      {}));
              });
            this.addAction(resources.svg['save'],()=>{
                const noteId=this.attrs.parent.state.match.noteId;
                const nd=this.attrs.parent.attrs.textarea.getDomNode();
                console.log(nd.value);
                this.attrs.parent.attrs.ctxt.setContent(this.attrs.parent.state.match.noteId,
                                  nd.value).then((result)=>{
                    router.navigate(router.routes.userNotesContent({noteId},{}));
                    
                  }).catch((error)=>{});
              });
          }
        }
        class EditFooter extends components.NavFooter {
          constructor(parent){
            super();
            this.attrs.parent=parent;
          }
        }
        class NoteEditPage extends DomElement {
          constructor(){
            super("div",{className:styles.page},[]);
            this.attrs={header:new EditHeader(this),footer:new EditFooter(this),container:new DomElement(
                              "div",{className:styles.contentDiv},[]),textarea:new DomElement("textarea",
                              {className:styles.contentText},[]),padding1:new DomElement("div",
                              {className:styles.padding1},[])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.padding1);
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.footer);
            this.attrs.container.appendChild(this.attrs.textarea);
            this.attrs.ctxt=initContext();
          }
          elementMounted(){
            this.showContent();
          }
          showContent(){
            this.attrs.textarea.removeChildren();
            this.attrs.ctxt.getContent(this.state.match.noteId).then(result=>{
                this.attrs.textarea.appendChild(new TextElement(result));
              }).catch(error=>{
                console.log(error);
              });
          }
        }
        return[NoteContentPage,NoteContext,NoteEditPage,NotesPage];
      })();
    const[PlaylistPage]=(function(){
        const style={main:'dcs-13113e22-0',toolbar:'dcs-13113e22-1',info:'dcs-13113e22-2',
                  songList:'dcs-13113e22-3',songItem:'dcs-13113e22-4',songItemPlaceholder:'dcs-13113e22-5',
                  songItemActive:'dcs-13113e22-6',fontBig:'dcs-13113e22-7',fontSmall:'dcs-13113e22-8',
                  songItemRow:'dcs-13113e22-9',songItemRhs:'dcs-13113e22-10',songItemRow2:'dcs-13113e22-11',
                  callbackLink2:'dcs-13113e22-12',grip:'dcs-13113e22-13',space5:'dcs-13113e22-14',
                  center80:'dcs-13113e22-15',centerRow:'dcs-13113e22-16',lockScreen:'dcs-13113e22-17',
                  padding1:'dcs-13113e22-18',padding2:'dcs-13113e22-19',progressBar:'dcs-13113e22-20',
                  progressBar_bar:'dcs-13113e22-21',progressBar_button:'dcs-13113e22-22'};
        
        ;
        ;
        ;
        ;
        function formatTime(secs){
          secs=secs===Infinity?0:secs;
          let minutes=Math.floor(secs/60)||0;
          let seconds=Math.floor(secs-minutes*60)||0;
          return minutes+':'+(seconds<10?'0':'')+seconds;
        }
        class CallbackLink2 extends DomElement {
          constructor(text,callback){
            super('div',{className:style.callbackLink2},[new TextElement(text)]);
            
            this.state={callback};
          }
          onClick(){
            this.state.callback();
          }
        }
        class SongItem extends DomElement {
          constructor(parent,index,song){
            super("div",{className:style.songItem},[]);
            this.attrs={parent,index,song,active:false};
            this.appendChild(new DomElement("div",{className:style.space5},[]));
            const grip=this.appendChild(new DomElement("div",{className:style.grip},
                              []));
            this.appendChild(new DomElement("div",{className:style.space5},[]));
            grip.props.onMouseDown=(event)=>{
              let node=this.getDomNode();
              node.style.width=node.clientWidth+'px';
              node.style.background="white";
              this.attrs.parent.handleChildDragBegin(this,event);
              event.stopPropagation();
            };
            grip.props.onTouchStart=(event)=>{
              let node=this.getDomNode();
              node.style.width=node.clientWidth+'px';
              node.style.background="white";
              this.attrs.parent.handleChildDragBegin(this,event);
              event.stopPropagation();
            };
            const divrhs=this.appendChild(new DomElement("div",{className:style.songItemRhs},
                              []));
            this.attrs.txt1=divrhs.appendChild(new components.MiddleText((index+1)+". "+song.title));
            
            this.attrs.txt1.addClassName(style.fontBig);
            const div=divrhs.appendChild(new DomElement("div",{},[]));
            this.attrs.txt2=div.appendChild(new components.MiddleText(song.artist));
            
            this.attrs.txt3=div.appendChild(new TextElement(formatTime(song.length)));
            
            div.addClassName(style.fontSmall);
            div.addClassName(style.songItemRow2);
          }
          setIndex(index){
            if(index!=this.attrs.index){
              this.attrs.index=index;
              this.attrs.txt1.setText((index+1)+". "+this.attrs.song.title);
            }
          }
          updateActive(active){
            if(this.attrs.active!=active){
              this.attrs.active=active;
              if(active===true){
                this.attrs.txt1.setText((this.attrs.index+1)+". *** "+this.attrs.song.title);
                
                this.addClassName(style.songItemActive);
                return">T";
              }else{
                this.removeClassName(style.songItemActive);
                this.attrs.txt1.setText((this.attrs.index+1)+". "+this.attrs.song.title);
                
                return">F";
              }
            }
            return">S";
          }
          onTouchStart(event){
            let node=this.getDomNode();
            node.style.width=node.clientWidth+'px';
            node.style.background="white";
            this.attrs.parent.handleChildSwipeBegin(this,event);
            event.stopPropagation();
          }
          onMouseDown(event){
            let node=this.getDomNode();
            node.style.width=node.clientWidth+'px';
            node.style.background="white";
            this.attrs.parent.handleChildSwipeBegin(this,event);
            event.stopPropagation();
          }
          onTouchMove(event){
            if(this.attrs.parent.attrs.isSwipe){
              if(!this.attrs.parent.handleChildSwipeMove(this,event)){
                return;
              }
            }else{
              this.attrs.parent.handleChildDragMove(this,event);
            }
            event.stopPropagation();
          }
          onTouchEnd(event){
            if(this.attrs.parent.attrs.isSwipe){
              this.attrs.parent.handleChildSwipeEnd(this,{target:this.getDomNode(
                                    )});
            }else{
              this.attrs.parent.handleChildDragEnd(this,{target:this.getDomNode()});
              
              let node=this.getDomNode();
              node.style.removeProperty('width');
              node.style.removeProperty('background');
            }
            event.stopPropagation();
          }
          onTouchCancel(event){
            if(this.attrs.parent.attrs.isSwipe){
              this.attrs.parent.handleChildSwipeEnd(this,{target:this.getDomNode(
                                    )});
            }else{
              this.attrs.parent.handleChildDragEnd(this,{target:this.getDomNode()});
              
              let node=this.getDomNode();
              node.style.removeProperty('width');
              node.style.removeProperty('background');
            }
            event.stopPropagation();
          }
          onMouseMove(event){
            if(this.attrs.parent.attrs.isSwipe){
              if(!this.attrs.parent.handleChildSwipeMove(this,event)){
                return;
              }
            }else{
              this.attrs.parent.handleChildDragMove(this,event);
            }
            event.stopPropagation();
          }
          onMouseLeave(event){
            if(this.attrs.parent.attrs.isSwipe){
              this.attrs.parent.handleChildSwipeCancel(this,event);
            }else{
              this.attrs.parent.handleChildDragMove(this,event);
              let node=this.getDomNode();
              node.style.removeProperty('width');
              node.style.removeProperty('background');
            }
            event.stopPropagation();
          }
          onMouseUp(event){
            if(this.attrs.parent.attrs.isSwipe){
              this.attrs.parent.handleChildSwipeEnd(this,event);
            }else{
              this.attrs.parent.handleChildDragEnd(this,event);
              let node=this.getDomNode();
              node.style.removeProperty('width');
              node.style.removeProperty('background');
            }
            event.stopPropagation();
          }
        }
        class ProgressBarTrack extends DomElement {
          constructor(parent){
            super("div",{className:style.progressBar_bar},[]);
          }
        }
        class ProgressBarButton extends DomElement {
          constructor(parent){
            super("div",{className:style.progressBar_button},[]);
          }
        }
        class ProgressBar extends DomElement {
          constructor(callback){
            super("div",{className:style.progressBar},[]);
            this.attrs={callback,pressed:false,pos:0,tpos:0,startx:0,track:this.appendChild(
                              new ProgressBarTrack()),btn:this.appendChild(new ProgressBarButton(
                                ))};
          }
          setPosition(position,count=1.0){
            let pos=0;
            if(count>0){
              pos=position/count;
            }
            this.attrs.pos=pos;
            const btn=this.attrs.btn.getDomNode();
            const ele=this.getDomNode();
            if(btn&&ele){
              let m2=(ele.clientWidth-btn.clientWidth);
              let m1=0;
              let x=m2*pos;
              if(x>m2){
                x=m2;
              }else if(x<m1){
                x=m1;
              }
              this.attrs.startx=Math.floor(x)+"px";
              if(!this.attrs.pressed){
                const btn=this.attrs.btn.getDomNode();
                btn.style.left=this.attrs.startx;
              }
            }
          }
          onMouseDown(event){
            this.trackingStart();
            this.trackingMove(event);
          }
          onMouseMove(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingMove(event);
          }
          onMouseLeave(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingEnd(false);
          }
          onMouseUp(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingMove(event);
            this.trackingEnd(true);
          }
          onTouchStart(event){
            this.trackingStart();
            this.trackingMove(event);
          }
          onTouchMove(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingMove(event);
          }
          onTouchCancel(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingEnd(false);
          }
          onTouchEnd(event){
            if(!this.attrs.pressed){
              return;
            }
            this.trackingMove(event);
            this.trackingEnd(true);
          }
          trackingStart(){
            const btn=this.attrs.btn.getDomNode();
            this.attrs.startx=btn.style.left;
            this.attrs.pressed=true;
          }
          trackingEnd(accept){
            const btn=this.attrs.btn.getDomNode();
            this.attrs.pressed=false;
            if(accept){
              if(this.attrs.callback){
                this.attrs.callback(this.attrs.tpos);
              }
            }else{
              btn.style.left=this.attrs.startx;
            }
          }
          trackingMove(event){
            let org_event=event;
            let evt=(((event)||{}).touches||((((event)||{}).originalEvent)||{}).touches);
            
            if(evt){
              event=evt[0];
            }
            if(!event){
              return;
            }
            const btn=this.attrs.btn.getDomNode();
            const ele=this.getDomNode();
            const rect=ele.getBoundingClientRect();
            let x=event.pageX-rect.left-(btn.clientWidth/2);
            let m2=ele.clientWidth-btn.clientWidth;
            let m1=0;
            if(x>m2){
              x=m2;
            }else if(x<m1){
              x=m1;
            }
            this.attrs.tpos=(m2>0&&x>=0)?x/m2:0.0;
            btn.style.left=Math.floor(x)+"px";
          }
        }
        class Header extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['media_prev'],()=>{
                audio.AudioDevice.instance().prev();
              });
            this.attrs.act_play_pause=this.addAction(resources.svg['media_play'],
                          ()=>{
                audio.AudioDevice.instance().togglePlayPause();
              });
            this.addAction(resources.svg['media_next'],()=>{
                audio.AudioDevice.instance().next();
              });
            this.addAction(resources.svg['save'],()=>{
                audio.AudioDevice.instance().queueSave();
              });
            this.attrs.txt_SongTitle=new components.MiddleText("Select A Song");
            this.attrs.txt_SongTime=new TextElement("00:00:00/00:00:00");
            this.attrs.txt_SongStatus=new TextElement("");
            this.attrs.pbar_time=new ProgressBar((pos)=>{
                let inst=audio.AudioDevice.instance();
                let dur=inst.duration();
                if(!!dur){
                  inst.setCurrentTime(pos*dur);
                }
              });
            this.addRow(true);
            this.addRow(true);
            this.addRow(true);
            this.addRow(true);
            this.addRowElement(0,this.attrs.txt_SongTitle);
            this.addRowElement(1,this.attrs.txt_SongTime);
            this.addRowElement(2,this.attrs.txt_SongStatus);
            this.addRowElement(3,this.attrs.pbar_time);
            this.attrs.txt_SongTime.props.onClick=()=>{
              const device=audio.AudioDevice.instance();
              device.setCurrentTime(device.duration()-2);
            };
          }
          setSong(song){
            if(song===null){
              this.attrs.txt_SongTitle.setText("Select A Song");
              this.attrs.txt_SongTitle.setText("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
              
            }else{
              console.log(`set song: ${song.artist+" - "+song.title}`);
              this.attrs.txt_SongTitle.setText(song.artist+" - "+song.title);
            }
          }
          setTime(currentTime,duration){
            try{
              const t1=formatTime(currentTime);
              const t2=formatTime(duration);
              this.attrs.txt_SongTime.setText(t1+"/"+t2);
              this.attrs.pbar_time.setPosition(currentTime,duration);
            }catch(e){
              console.error(e);
            };
          }
          setStatus(status){
            this.attrs.txt_SongStatus.setText(status);
            if(status==="playing"){
              this.attrs.act_play_pause.setUrl(resources.svg['media_pause']);
            }else{
              this.attrs.act_play_pause.setUrl(resources.svg['media_play']);
            }
          }
        }
        const SWIPE_RIGHT=0x01;
        const SWIPE_LEFT=0x02;
        class SongList extends daedalus.DraggableList {
          constructor(){
            super();
            this.attrs.isSwipe=false;
            this.attrs.isAnimated=false;
            this.attrs.swipeActionRight=null;
            this.attrs.swipeActionLeft=null;
            this.attrs.swipeActionCancel=null;
            this.attrs.swipeConfig=SWIPE_RIGHT;
          }
          updateModel(indexStart,indexEnd){
            super.updateModel(indexStart,indexEnd);
            audio.AudioDevice.instance().queueSwapSong(indexStart,indexEnd);
          }
          handleChildDragBegin(child,event){
            if(this.attrs.isAnimated){
              return;
            }
            super.handleChildDragBegin(child,event);
            this.attrs.isSwipe=false;
          }
          handleChildDragMove(child,event){
            if(this.attrs.isAnimated){
              return;
            }
            super.handleChildDragMove(child,event);
            this.attrs.isSwipe=false;
          }
          handleChildSwipeBegin(child,event){
            if(this.attrs.isAnimated){
              return;
            }
            if(!!this.attrs.draggingEle){
              this.handleChildSwipeCancel(child,event);
              return;
            }
            const org_event=event;
            let evt=(((event)||{}).touches||((((event)||{}).originalEvent)||{}).touches);
            
            if(evt){
              event=evt[0];
            }
            const draggingEle=child.getDomNode();
            const rect=draggingEle.getBoundingClientRect();
            const x=event.pageX-rect.left;
            const y=event.pageY-rect.top;
            let pos=Math.abs(Math.floor(100*(x/(rect.right-rect.left))));
            if(pos>30&&pos<70){
              org_event.preventDefault();
              this.attrs.draggingEle=draggingEle;
              this.attrs.xstart=rect.left;
              this.attrs.ystart=rect.top;
              this.attrs.x=x;
              this.attrs.y=y;
              this.attrs.isSwipe=true;
            }
          }
          handleChildSwipeMove(child,event){
            if(this.attrs.isAnimated){
              return;
            }
            if(!this.attrs.draggingEle||this.attrs.draggingEle!==child.getDomNode(
                            )){
              return;
            }
            let org_event=event;
            let evt=(((event)||{}).touches||((((event)||{}).originalEvent)||{}).touches);
            
            if(evt){
              event=evt[0];
              if(event.pageY<this.attrs.draggingEle.offsetTop||event.pageY>this.attrs.draggingEle.offsetTop+this.attrs.draggingEle.clientHeight){
              
                this.handleChildSwipeCancel(child,event);
                return;
              }
            }
            let deltax=event.pageX-this.attrs.xstart-this.attrs.x;
            if(!this.attrs.isDraggingStarted){
              const draggingRect=this.attrs.draggingEle.getBoundingClientRect();
              if(Math.abs(deltax)<32){
                return false;
              }
              this.attrs.isDraggingStarted=true;
              this.attrs.draggingEle.style.removeProperty('transition');
              this.attrs.placeholder=document.createElement('div');
              this.attrs.placeholder.classList.add(this.attrs.placeholderClassName);
              
              this.attrs.draggingEle.parentNode.insertBefore(this.attrs.placeholder,
                              this.attrs.draggingEle.nextSibling);
              this.attrs.placeholder.style.height=`${draggingRect.height-2}px`;
            }
            org_event.preventDefault();
            this.attrs.draggingEle.style.position='absolute';
            this.attrs.draggingEle.style.left=`${event.pageX-this.attrs.x}px`;
            return true;
          }
          handleChildSwipeEnd(child,event){
            this.handleChildSwipeCancel(child,event,true);
          }
          handleChildSwipeCancel(child,event,success=false){
            if(!this.attrs.draggingEle||this.attrs.draggingEle!==child.getDomNode(
                            )){
              return;
            }
            if(!this.attrs.placeholder){
              return;
            }
            if(this.attrs.isAnimated){
              return;
            }
            let deltax=this.attrs.draggingEle.offsetLeft-this.attrs.placeholder.offsetLeft;
            
            const SWIPE_OFFSET=32;
            const cfg=this.attrs.swipeConfig;
            if(success&&deltax>SWIPE_OFFSET&&cfg&SWIPE_RIGHT){
              this.attrs.draggingEle.style.left=`${document.body.clientWidth}px`;
              
              this.swipeActionRight=child;
            }else if(success&&deltax<SWIPE_OFFSET&&cfg&SWIPE_LEFT){
              this.attrs.draggingEle.style.left=`${-this.attrs.draggingEle.clientWidth}px`;
              
              this.swipeActionLeft=child;
            }else{
              this.attrs.draggingEle.style.left=this.attrs.placeholder.offsetLeft+'px';
              
              if(success){
                this.swipeActionCancel=child;
              }
            }
            this.attrs.draggingEle.style.transition='left .35s';
            setTimeout(this.handleChildSwipeTimeout.bind(this),350);
            this.attrs.isAnimated=true;
          }
          handleChildSwipeTimeout(){
            this.attrs.isAnimated=false;
            this.attrs.x=null;
            this.attrs.y=null;
            this.attrs.isDraggingStarted=false;
            if(this.attrs.placeholder&&this.attrs.placeholder.parentNode){
              this.attrs.placeholder.parentNode.removeChild(this.attrs.placeholder);
              
            }
            if(!this.attrs.draggingEle){
              return;
            }
            this.attrs.draggingEle.style.removeProperty('left');
            this.attrs.draggingEle.style.removeProperty('position');
            this.attrs.draggingEle.style.removeProperty('transition');
            this.attrs.draggingEle.style.removeProperty('width');
            this.attrs.draggingEle.style.removeProperty('background');
            this.attrs.draggingEle=null;
            if(!!this.swipeActionRight){
              console.log("swipe action right");
              this.handleSwipeRight(this.swipeActionRight);
              this.swipeActionRight=null;
            }
            if(!!this.swipeActionLeft){
              console.log("swipe action left");
              this.handleSwipeLeft(this.swipeActionLeft);
              this.swipeActionLeft=null;
            }
            if(!!this.swipeActionCancel){
              console.log("swipe action cancel");
              this.handleSwipeCancel(this.swipeActionCancel);
              this.swipeActionCancel=null;
            }
          }
          handleSwipeRight(child){
            console.log("handle swipe right",child.attrs.index);
            const index=child.attrs.index;
            audio.AudioDevice.instance().queueRemoveIndex(index);
          }
          handleSwipeLeft(child){
            console.log("handle swipe left");
          }
          handleSwipeCancel(child){
            console.log("handle swipe cancel");
            const index=child.attrs.index;
            audio.AudioDevice.instance().playIndex(index);
          }
        }
        class PlaylistPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={device:audio.AudioDevice.instance(),header:new Header(this),
                          container:new SongList(),padding1:new DomElement("div",{className:style.padding1},
                              []),padding2:new DomElement("div",{className:style.padding2},[]),
                          currentIndex:-1};
            this.attrs.container.setPlaceholderClassName(style.songItemPlaceholder);
            
            this.attrs.container.addClassName(style.songList);
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.padding1);
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.padding2);
          }
          elementMounted(){
            console.log("mount playlist view");
            this.attrs.device.connectView(this);
            if(this.attrs.device.queueLength()==0){
              console.log(`playlist mounted. reload queue`);
              this.attrs.device.queueLoad();
            }else{
              console.log(`playlist mounted. current_index: ${this.attrs.device.current_index}`);
              
              const song=this.attrs.device.currentSong();
              this.attrs.header.setSong(song);
              this.handleAudioQueueChanged(this.attrs.device.queue);
              console.log(`playlist mount complete`);
            }
            if(daedalus.platform.isAndroid){
              registerAndroidEvent('onresume',this.handleResume.bind(this));
            }
          }
          elementUnmounted(){
            console.log("dismount playlist view");
            this.attrs.device.disconnectView(this);
            if(daedalus.platform.isAndroid){
              registerAndroidEvent('onresume',()=>{});
            }
          }
          handleAudioPlay(event){
            this.attrs.header.setStatus("playing");
          }
          handleAudioPause(event){
            this.attrs.header.setStatus("paused");
          }
          handleAudioWaiting(event){
            this.attrs.header.setStatus("waiting");
          }
          handleAudioStalled(event){
            this.attrs.header.setStatus("stalled");
          }
          handleAudioEnded(event){
            this.attrs.header.setStatus("ended");
          }
          handleAudioError(event){
            this.attrs.header.setStatus("error");
          }
          handleAudioTimeUpdate(event){
            this.attrs.header.setTime(event.currentTime,event.duration);
          }
          handleAudioDurationChange(event){
            this.attrs.header.setTime(event.currentTime,event.duration);
          }
          handleAudioSongChanged(song){
            this.attrs.header.setSong(song);
            if(song!==null){
              if(!song.id){
                this.attrs.header.setStatus("load error: invalid id");
              }else{
                this.attrs.currentIndex=song.index;
                this.attrs.header.setStatus("pending");
                this.attrs.container.children.forEach((child,index)=>{
                    child.updateActive(index===song.index);
                  });
                this.attrs.header.setTime(0,0);
              }
            }else{
              this.attrs.header.setStatus("load error: null");
            }
          }
          handleAudioQueueChanged(songList){
            const current_id=audio.AudioDevice.instance().currentSongId();
            const current_index=audio.AudioDevice.instance().currentSongIndex();
            console.log(`handleAudioQueueChanged: ${this.attrs.device.current_index+1}/${this.attrs.device.queue.length}::${current_id}`);
            
            let miss=0;
            let hit=0;
            let del=0;
            let index=0;
            let item=null;
            const containerList=this.attrs.container.children;
            const N=containerList.length<songList.length?containerList.length:songList.length;
            
            for(;index<containerList.length&&index<songList.length;index++)
            {
              if(containerList[index].attrs.song.id==songList[index].id){
                item=containerList[index];
                item.setIndex(index);
              }else if(index<(containerList.length-1)&&containerList[index+1].attrs.song.id==songList[
                                index].id){
                containerList.splice(index,1);
                item=containerList[index];
                item.setIndex(index);
                del+=1;
              }else{
                miss+=1;
                item=new SongItem(this.attrs.container,index,songList[index]);
                containerList[index]=item;
              }
              item.updateActive(index===current_index);
            }
            const removeCount=containerList.length-index;
            console.log("update",containerList.length,songList.length,removeCount,
                          index);
            if(removeCount>0){
              containerList.splice(index,removeCount);
              del+=removeCount;
            }
            for(;index<songList.length;index++)
            {
              item=new SongItem(this.attrs.container,index,songList[index]);
              containerList.push(item);
              item.updateActive(index===current_index);
              miss+=1;
            }
            if(miss>0||del>0){
              this.attrs.container.update();
            }
            console.log("miss rate",hit,miss,del);
          }
          handleResume(){
            console.log("on app resume");
          }
        }
        return[PlaylistPage];
      })();
    const[SettingsPage]=(function(){
        const style={main:'dcs-6349d093-0',settingsItem:'dcs-6349d093-1',settingsRowItem:'dcs-6349d093-2'};
        
        class SettingsItem extends DomElement {
          constructor(title){
            super("div",{className:style.settingsItem},[]);
            this.appendChild(new TextElement(title));
          }
        }
        class SettingsButtonItem extends DomElement {
          constructor(title,action){
            super("div",{className:style.settingsRowItem},[]);
            this.attrs.count=0;
            this.appendChild(new ButtonElement(title,()=>{
                  action(this);
                }));
          }
          setText(text){

          }
        }
        class SettingsToggleRangeItem extends DomElement {
          constructor(title,action,action2){
            super("div",{className:style.settingsRowItem},[]);
            this.attrs.count=0;
            this.appendChild(new ButtonElement(title,()=>{
                  action(this);
                }));
            this.appendChild(new DomElement("input",{min:0,max:100,value:50,type:"range",
                                  onchange:action2}));
          }
          setText(text){

          }
        }
        class SettingsGroupItem extends DomElement {
          constructor(title,names){
            super("div",{className:style.settingsItem},[]);
            this.appendChild(new TextElement(title));
            this.appendChild(new DomElement("br",{},[]));
            const form=this.appendChild(new DomElement("form",{},[]));
            names.forEach(name=>{
                const child=form.appendChild(new DomElement("div",{},[]));
                const btn=child.appendChild(new DomElement("input",{type:"radio",
                                          value:name,name:this.props.id}));
                child.appendChild(new DomElement("label",{'forx':btn.props.id},[new TextElement(
                                              name)]));
              });
          }
        }
        class Header extends components.NavHeader {
          constructor(parent){
            super();
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
          }
        }
        class SettingsPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={header:new Header(this),container:new DomElement("div",{},
                              [])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.container);
            this.attrs.container.appendChild(new SettingsItem("Volume:"));
            this.attrs.container.appendChild(new SettingsGroupItem("Audio Backend:",
                              ["Cloud","Cloud Native","Native"]));
            this.attrs.container.appendChild(new SettingsButtonItem("file api test",
                              (item)=>{
                  if(Client){
                    console.log("test");
                    item.attrs.count+=1;
                    if(Client.fileExists("sample.dat")){
                      item.setText(item.attrs.count+" : "+"T");
                    }else{
                      item.setText(item.attrs.count+" : "+"F");
                      const url="http://192.168.1.149:4100/static/index.js";
                      const folder='Music/test';
                      const name='index.js';
                      Client.downloadUrl(url,folder,name);
                    }
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("load",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    const url1="https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
                    
                    const url2="https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3";
                    
                    const url3="https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3";
                    
                    const queue=[{url:url1},{url:url2},{url:url3}];
                    const data=JSON.stringify(queue);
                    console.log(data);
                    AndroidNativeAudio.setQueue(data);
                    AndroidNativeAudio.loadIndex(0);
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("play",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    AndroidNativeAudio.play();
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("pause",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    AndroidNativeAudio.pause();
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("next",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    AndroidNativeAudio.skipToNext();
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("prev",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    AndroidNativeAudio.skipToPrev();
                  }
                }));
            this.attrs.container.appendChild(new SettingsButtonItem("fetch",(item)=>{
                
                  if(daedalus.platform.isAndroid){
                    AndroidNativeAudio.beginFetch(""+api.getAuthToken());
                  }
                }));
            this.attrs.noise=[new audio.WhiteNoiseContext(),new audio.PinkNoiseContext(
                            ),new audio.BrownNoiseContext(),new audio.OceanNoiseContext()];
            for(let i=0;i<this.attrs.noise.length;i++)
            {
              let noise=this.attrs.noise[i];
              this.attrs.container.appendChild(new SettingsToggleRangeItem(noise.color+" noise",
                                  (item)=>{
                    if(noise.isPlaying()){
                      console.log("pause "+noise.color);
                      noise.pause();
                    }else{
                      console.log("play "+noise.color);
                      noise.play();
                    }
                  },(event)=>{
                    noise.setVolume(event.target.value/100);
                  }));
            }
          }
        }
        return[SettingsPage];
      })();
    const[LibraryPage,SavedSearchPage,SyncPage]=(function(){
        class SearchBannishedCheckBox extends components.CheckBoxElement {
          onClick(event){
            this.attrs.callback();
          }
          getStateIcons(){
            return[resources.svg.checkbox_unchecked,resources.svg.checkbox_checked];
            
          }
        }
        class SearchModeCheckBox extends components.CheckBoxElement {
          onClick(event){
            this.attrs.callback();
          }
          getStateIcons(){
            return[resources.svg.checkbox_unchecked,resources.svg.checkbox_synced,
                          resources.svg.checkbox_not_synced,resources.svg.checkbox_partial];
          }
        }
        class SyncCheckBox extends components.CheckBoxElement {
          onClick(event){
            this.attrs.callback();
          }
          getStateIcons(){
            return[resources.svg.checkbox_unchecked,resources.svg.checkbox_download,
                          resources.svg.checkbox_partial];
          }
        }
        const style={main:'dcs-f089c6c5-0',grow:'dcs-f089c6c5-1',viewPad:'dcs-f089c6c5-2',
                  listItemCheck:'dcs-f089c6c5-3',savedSearchPage:'dcs-f089c6c5-4',savedSearchList:'dcs-f089c6c5-5',
                  savedSearchItem:'dcs-f089c6c5-6',padding1:'dcs-f089c6c5-7',padding2:'dcs-f089c6c5-8'};
        
        function shuffle(a){
          for(let i=a.length-1;i>0;i--)
          {
            const j=Math.floor(Math.random()*(i+1));
            [a[i],a[j]]=[a[j],a[i]];
          }
          return a;
        }
        const sort_order=['year','album','album_index'];
        function sortTracks(tracks,order=sort_order){
          tracks.sort((a,b)=>{
              return order.map((k)=>{
                  const av=a[k];
                  const bv=b[k];
                  return(av==bv)?0:(av<bv)?-1:1;
                }).reduce((p,n)=>p?p:n,0);
            });
          return tracks;
        }
        class Header extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.attrs.txtInput=new TextInputElement("",null,()=>{
                this.attrs.parent.search(this.attrs.txtInput.props.value);
              });
            this.attrs.txtInput.updateProps({"autocapitalize":"off"});
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['media_prev'],()=>{
                audio.AudioDevice.instance().prev();
              });
            this.addAction(resources.svg['media_play'],()=>{
                audio.AudioDevice.instance().togglePlayPause();
              });
            this.addAction(resources.svg['media_next'],()=>{
                audio.AudioDevice.instance().next();
              });
            this.addRow(false);
            this.addRowElement(0,this.attrs.txtInput);
            this.attrs.txtInput.addClassName(style.grow);
            if(daedalus.platform.isAndroid){
              this.attrs.chk=new SearchModeCheckBox(this.handleCheck.bind(this),1);
              
              this.addRowElement(0,new components.HSpacer("1em"));
              this.addRowElement(0,this.attrs.chk);
              this.addRowElement(0,new components.HSpacer("1em"));
            }
            this.attrs.show_banished=new SearchBannishedCheckBox(this.handleCheckShowBannished.bind(
                              this),0);
            this.addRowElement(0,new components.HSpacer("1em"));
            this.addRowElement(0,this.attrs.show_banished);
            this.addRowElement(0,new components.HSpacer("1em"));
            this.addRowAction(0,resources.svg['search'],()=>{
                this.attrs.parent.search(this.attrs.txtInput.props.value);
              });
          }
          setQuery(query){
            this.attrs.txtInput.setText(query);
          }
          handleCheck(){
            this.attrs.chk.setCheckState((this.attrs.chk.attrs.checkState+1)%3);
          }
          handleCheckShowBannished(){
            this.attrs.show_banished.setCheckState((this.attrs.show_banished.attrs.checkState+1)%2);
            
          }
          syncState(){
            return this.attrs.chk.attrs.checkState;
          }
          showBanished(){
            return this.attrs.show_banished.attrs.checkState;
          }
        }
        class Footer extends components.NavFooter {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['select'],()=>{
                const count=this.attrs.parent.attrs.view.countSelected();
                this.attrs.parent.attrs.view.selectAll(count==0);
              });
            this.addAction(resources.svg['sort'],()=>{
                const songList=this.attrs.parent.attrs.view.getSelectedSongs();
                console.log("creating playlist",songList.length);
                audio.AudioDevice.instance().queueSet(sortTracks(songList).splice(
                                      0,100));
                audio.AudioDevice.instance().next();
                this.attrs.parent.attrs.view.selectAll(false);
              });
            this.addAction(resources.svg['media_shuffle'],()=>{
                const songList=this.attrs.parent.attrs.view.getSelectedSongs();
                console.log("creating playlist",songList.length);
                audio.AudioDevice.instance().queueSet(shuffle(songList).splice(0,
                                      100));
                audio.AudioDevice.instance().next();
                this.attrs.parent.attrs.view.selectAll(false);
              });
          }
        }
        class ArtistTreeItem extends components.TreeItem {
          constructor(parent,obj,selectMode=1){
            let selected=0;
            if(selectMode==components.TreeItem.SELECTION_MODE_CHECK){
              selected=obj.selected||0;
            }
            super(parent,0,obj.name,obj,selectMode,selected);
          }
          buildChildren(obj){
            return obj.albums.map(album=>new AlbumTreeItem(this,album,this.attrs.selectMode));
            
          }
          constructCheckbox(callback,initialState){
            return new SyncCheckBox(callback,initialState);
          }
        }
        class AlbumTreeItem extends components.TreeItem {
          constructor(parent,obj,selectMode=1){
            let selected=0;
            if(selectMode==components.TreeItem.SELECTION_MODE_CHECK){
              selected=obj.selected||0;
            }
            super(parent,1,obj.name,obj,selectMode,selected);
          }
          buildChildren(obj){
            return obj.tracks.map(track=>new TrackTreeItem(this,track,this.attrs.selectMode));
            
          }
          constructCheckbox(callback,initialState){
            return new SyncCheckBox(callback,initialState);
          }
        }
        class TrackTreeItem extends components.TreeItem {
          constructor(parent,obj,selectMode=1){
            let selected=0;
            if(selectMode==components.TreeItem.SELECTION_MODE_CHECK){
              selected=obj.sync||0;
            }
            super(parent,2,obj.title,obj,selectMode,selected);
            this.setMoreCallback(this.handleMoreClicked.bind(this));
          }
          hasChildren(){
            return false;
          }
          handleMoreClicked(){
            const abm=this.attrs.parent;
            const art=abm.attrs.parent;
            const view=art.attrs.parent;
            const page=view.attrs.parent;
            const song={...this.attrs.obj,artist:art.attrs.obj.name,album:abm.attrs.obj.name};
            
            console.log(art.attrs.parent);
            page.showMore(song);
          }
          constructCheckbox(callback,initialState){
            return new SyncCheckBox(callback,initialState);
          }
        }
        class LibraryTreeView extends components.TreeView {
          constructor(parent,selectMode){
            super();
            this.attrs.parent=parent;
            this.attrs.selectMode=selectMode;
          }
          setForest(forest){
            forest.forEach(tree=>{
                this.addItem(new ArtistTreeItem(this,tree,this.attrs.selectMode));
                
              });
          }
          getSelectedSongs(){
            const result=[];
            this.attrs.container.children.forEach(child=>{
                this._chkArtistSelection(result,child);
              });
            return result;
          }
          _chkArtistSelection(result,node){
            if(!node.attrs.children){
              this._collectArtist(result,node.attrs.obj,node.isSelected());
              return;
            }
            node.attrs.children.forEach(child=>{
                this._chkAlbumSelection(result,child,node.attrs.obj.name);
              });
          }
          _collectArtist(result,obj,selected){
            obj.albums.forEach(child=>{
                this._collectAlbum(result,child,obj.name,selected);
              });
          }
          _chkAlbumSelection(result,node,artist){
            if(!node.attrs.children){
              this._collectAlbum(result,node.attrs.obj,artist,node.isSelected());
              
              return;
            }
            node.attrs.children.forEach(child=>{
                this._chkTrackSelection(result,child,artist,node.attrs.obj.name);
                
              });
          }
          _collectAlbum(result,obj,artist,selected){
            obj.tracks.forEach(child=>{
                this._collectTrack(result,child,artist,obj.name,selected);
              });
          }
          _chkTrackSelection(result,node,artist,album){
            if(this.attrs.selectMode==components.TreeItem.SELECTION_MODE_CHECK){
              const item=node.attrs.obj;
              if(item.sync==1&&node.attrs.selected==0){
                const track={"spk":item.spk,sync:0};
                result.push(track);
              }else if(item.sync==0&&node.attrs.selected==1){
                const track={"spk":item.spk,sync:1};
                result.push(track);
              }
            }else{
              if(node.isSelected()){
                const song={...node.attrs.obj,artist,album};
                result.push(song);
              }
            }
          }
          _collectTrack(result,obj,artist,album,selected){
            if(this.attrs.selectMode==components.TreeItem.SELECTION_MODE_CHECK){
              if(obj.sync==0&&selected==1){
                const track={"uid":obj.id,"spk":obj.spk,sync:1};
                result.push(track);
              }
              if(obj.sync==1&&selected==0){
                const track={"uid":obj.id,"spk":obj.spk,sync:0};
                result.push(track);
              }
            }else{
              if(selected){
                const song={...obj,artist,album};
                result.push(song);
              }
            }
          }
        }
        class LibraryPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={header:new Header(this),footer:new Footer(this),view:new LibraryTreeView(
                              this,components.TreeItem.SELECTION_MODE_HIGHLIGHT),more:new components.MoreMenu(
                              this.handleHideFileMore.bind(this)),more_context_item:null,firstMount:true,
                          currentSearch:null};
            this.attrs.view.addClassName(style.viewPad);
            this.attrs.more.addAction("Add To Queue",this.handleAddToQueue.bind(this));
            
            this.appendChild(this.attrs.more);
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.view);
            this.appendChild(this.attrs.footer);
          }
          elementMounted(){
            console.log("mount library view");
            let query=daedalus.util.parseParameters()['query'];
            if(query===null||query===undefined){
              query="";
            }else{
              query=""+query;
            }
            if(this.attrs.firstMount||(this.attrs.currentSearch!==query)){
              this.attrs.firstMount=false;
              this.attrs.header.setQuery(query);
              this.search(query);
            }
          }
          search(text){
            this.attrs.view.reset();
            this.attrs.currentSearch=text;
            router.navigate(router.routes.userLibraryList({},{query:text}));
            this.attrs.search_promise=new Promise((accept,reject)=>{
                let showBanished=this.attrs.header.showBanished()===1;
                console.log(showBanished);
                if(daedalus.platform.isAndroid){
                  let syncState=this.attrs.header.syncState();
                  let payload=AndroidNativeAudio.buildForest(text,syncState,showBanished);
                  
                  let forest=JSON.parse(payload);
                  this.attrs.view.setForest(forest);
                }else{
                  api.librarySearchForest(text,showBanished).then(result=>{
                      this.attrs.view.setForest(result.result);
                    }).catch(error=>{
                      console.log(error);
                    });
                }
                accept();
              });
          }
          showMore(item){
            this.attrs.more_context_item=item;
            this.attrs.more.show();
          }
          handleHideFileMore(){
            this.attrs.more.hide();
          }
          handleAddToQueue(){
            audio.AudioDevice.instance().queuePlayNext(this.attrs.more_context_item);
            
          }
        }
        class SyncHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.attrs.txtInput=new TextInputElement("",null,()=>{
                this.attrs.parent.search(this.attrs.txtInput.props.value);
              });
            this.attrs.txtInput.updateProps({"autocapitalize":"off"});
            this.attrs.status=new components.MiddleText("...");
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
            this.addAction(resources.svg['media_error'],()=>{
                if(daedalus.platform.isAndroid){
                  AndroidNativeAudio.cancelTask();
                }
              });
            this.addAction(resources.svg['sort'],()=>{
                if(daedalus.platform.isAndroid){
                  AndroidNativeAudio.beginFetch(""+api.getAuthToken());
                }
              });
            this.addAction(resources.svg['search_generic'],()=>{
                this.attrs.parent.search();
              });
            this.addAction(resources.svg['save'],()=>{
                this.attrs.parent.handleSyncSave();
              });
            this.addAction(resources.svg['download'],()=>{
                if(daedalus.platform.isAndroid){
                  AndroidNativeAudio.beginSync(""+api.getAuthToken());
                }
              });
            this.addRow(false);
            this.addRowElement(0,this.attrs.txtInput);
            this.attrs.txtInput.addClassName(style.grow);
            if(daedalus.platform.isAndroid){
              this.attrs.chk=new SearchModeCheckBox(this.handleCheck.bind(this),0);
              
              this.addRowElement(0,new components.HSpacer("1em"));
              this.addRowElement(0,this.attrs.chk);
              this.addRowElement(0,new components.HSpacer("1em"));
            }
            this.addRowAction(0,resources.svg['search'],()=>{
                this.attrs.parent.search(this.attrs.txtInput.props.value);
              });
            this.addRow(false);
            this.addRowElement(1,this.attrs.status);
          }
          updateStatus(text){
            this.attrs.status.setText(text);
          }
          searchText(){
            return this.attrs.txtInput.props.value;
          }
          handleCheck(){
            this.attrs.chk.setCheckState((this.attrs.chk.attrs.checkState+1)%3);
          }
          syncState(){
            return this.attrs.chk.attrs.checkState;
          }
        }
        class SyncFooter extends components.NavFooter {
          constructor(parent){
            super();
            this.attrs.parent=parent;
          }
        }
        class SyncPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={header:new SyncHeader(this),footer:new SyncFooter(this),view:new LibraryTreeView(
                              this,components.TreeItem.SELECTION_MODE_CHECK),more:new components.MoreMenu(
                              this.handleHideFileMore.bind(this)),more_context_item:null,firstMount:true};
            
            this.attrs.view.addClassName(style.viewPad);
            this.attrs.more.addAction("Add To Queue",()=>{});
            this.appendChild(this.attrs.more);
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.view);
            this.appendChild(this.attrs.footer);
            this.attrs.footer_lbl1=this.attrs.footer.addText("");
          }
          elementMounted(){
            console.log("mount sync view");
            if(this.attrs.firstMount){
              this.attrs.firstMount=false;
              this.search("");
            }
            if(daedalus.platform.isAndroid){
              registerAndroidEvent('onfetchprogress',this.handleFetchProgress.bind(
                                  this));
              registerAndroidEvent('onfetchcomplete',this.handleFetchComplete.bind(
                                  this));
              registerAndroidEvent('onsyncstatusupdated',this.handleSyncStatusUpdated.bind(
                                  this));
              registerAndroidEvent('onsyncprogress',this.handleSyncProgress.bind(
                                  this));
              registerAndroidEvent('onsynccomplete',this.handleSyncComplete.bind(
                                  this));
              registerAndroidEvent('onresume',this.handleResume.bind(this));
              this.updateInfo();
            }
          }
          elementUnmounted(){
            console.log("unmount sync view");
            if(daedalus.platform.isAndroid){
              registerAndroidEvent('onfetchprogress',()=>{});
              registerAndroidEvent('onfetchcomplete',()=>{});
              registerAndroidEvent('onsyncstatusupdated',()=>{});
              registerAndroidEvent('onsyncprogress',()=>{});
              registerAndroidEvent('onsynccomplete',()=>{});
              registerAndroidEvent('onresume',()=>{});
            }
          }
          handleHideFileMore(){

          }
          search(text){
            this.attrs.view.reset();
            this.attrs.search_promise=new Promise((accept,reject)=>{
                if(daedalus.platform.isAndroid){
                  let syncState=this.attrs.header.syncState();
                  let showBanished=false;
                  let payload=AndroidNativeAudio.buildForest(text,syncState,showBanished);
                  
                  let forest=JSON.parse(payload);
                  this.attrs.view.setForest(forest);
                }else{
                  let showBanished=false;
                  api.librarySearchForest(text,showBanished).then(result=>{
                      this.attrs.view.setForest(result.result);
                    }).catch(error=>{
                      console.log(error);
                    });
                }
                accept();
              });
          }
          handleSyncSave(){
            let items=this.attrs.view.getSelectedSongs();
            console.log(JSON.stringify(items));
            console.log(`selected ${items.length} items`);
            let data={};
            for(let i=0;i<items.length;i++)
            {
              let item=items[i];
              data[item.spk]=item.sync;
            }
            console.log(JSON.stringify(data));
            console.log(`selected ${data.length} items`);
            if(items.length>0){
              if(daedalus.platform.isAndroid){
                let payload=JSON.stringify(data);
                AndroidNativeAudio.updateSyncStatus(payload);
              }else{
                console.log(data);
              }
            }else{
              console.log("sync save: nothing to save");
            }
          }
          handleFetchProgress(payload){
            this.attrs.header.updateStatus(`${payload.count}/${payload.total}`);
          }
          handleFetchComplete(payload){
            console.log("fetch complete: "+JSON.stringify(payload));
            this.updateInfo();
          }
          handleSyncProgress(payload){
            this.attrs.header.updateStatus(`${payload.index}/${payload.total} ${payload.message}`);
            
          }
          handleSyncComplete(payload){
            console.log("fetch complete: "+JSON.stringify(payload));
            this.attrs.header.updateStatus("sync complete");
            this.updateInfo();
          }
          handleSyncStatusUpdated(payload){
            this.search(this.attrs.header.searchText());
          }
          handleResume(payload){
            console.log("app resumed from js");
            if(daedalus.platform.isAndroid){
              AndroidNativeAudio.syncQueryStatus();
            }
          }
          showMore(item){
            console.log("on show more clicked");
          }
          updateInfo(){
            if(daedalus.platform.isAndroid){
              const info=JSON.parse(AndroidNativeAudio.getSyncInfo());
              this.attrs.footer_lbl1.setText(`records: ${info.record_count} synced: ${info.synced_tracks}`);
              
            }
          }
        }
        class SavedSearchHeader extends components.NavHeader {
          constructor(parent){
            super();
            this.attrs.parent=parent;
            this.addAction(resources.svg['menu'],()=>{
                store.globals.showMenu();
              });
          }
        }
        class SavedSearchItem extends DomElement {
          constructor(name,query){
            super("div",{className:style.savedSearchItem},[]);
            this.attrs={name,query};
            this.appendChild(new DomElement("div",{},[new TextElement(name)]));
            this.appendChild(new DomElement("div",{},[new TextElement(query)]));
          }
          onClick(event){
            router.navigate(router.routes.userLibraryList({},{query:this.attrs.query}));
            
          }
        }
        const savedSearches=[{name:"stoner best",query:"stoner rating >= 5"},{name:"grunge best",
                      query:"grunge rating >= 5"},{name:"visual best",query:"\"visual kei\" rating >= 5"},
                  {name:"english best",query:"language = english rating >= 5"},{name:"stone temple pilots",
                      query:"\"stone temple pilots\""},{name:"soundwitch",query:"soundwitch"},
                  {name:"Gothic Emily",query:"\"gothic emily\""},{name:"Driving Hits Volume 1",
                      query:"\":DRV\" p lt -14d"},{name:"Driving Hits Volume 2",query:"\":VL2\" p lt -14d"},
                  {name:"Driving Hits Volume 3",query:"comment=\":DRV\" or comment=\":VL2\" p lt -14d"}];
        
        class SavedSearchList extends DomElement {
          constructor(parent,index,song){
            super("div",{className:style.savedSearchList},[]);
            for(let i=0;i<savedSearches.length;i++)
            {
              let s=savedSearches[i];
              this.appendChild(new SavedSearchItem(s.name,s.query));
            }
          }
        }
        class SavedSearchPage extends DomElement {
          constructor(){
            super("div",{className:style.savedSearchPage},[]);
            this.attrs={device:audio.AudioDevice.instance(),header:new SavedSearchHeader(
                              this),container:new SavedSearchList(),padding1:new DomElement("div",
                              {className:style.padding1},[]),padding2:new DomElement("div",{className:style.padding2},
                              [])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.padding1);
            this.appendChild(this.attrs.container);
            this.appendChild(this.attrs.padding2);
          }
        }
        return[LibraryPage,SavedSearchPage,SyncPage];
      })();
    const[UserRadioPage]=(function(){
        const style={main:'dcs-a4af2c4a-0',header:'dcs-a4af2c4a-1'};
        class Header extends DomElement {
          constructor(parent){
            super("div",{className:style.header},[]);
            this.appendChild(new components.MiddleText("No Soap Radio"));
          }
        }
        class UserRadioPage extends DomElement {
          constructor(){
            super("div",{className:style.main},[]);
            this.attrs={header:new Header(this),container:new DomElement("div",{},
                              [])};
            this.appendChild(this.attrs.header);
            this.appendChild(this.attrs.container);
          }
          elementMounted(){
            console.log("mount radio view");
            api.radioVideoInfo("VHfi4kGPFvc").then(result=>{
                console.log(result);
              }).catch(error=>{
                console.log(error);
              });
          }
        }
        return[UserRadioPage];
      })();
    const[OpenApiDocPage]=(function(){
        const styles={main:'dcs-71d9fd06-0'};
        class OpenApiDocPage extends DomElement {
          constructor(){
            super("div",{className:styles.main},[]);
            this.doc=this.appendChild(new DomElement("pre"));
          }
          elementMounted(){
            api.userDoc(location.origin).then((result)=>{
                this.doc.appendChild(new TextElement(result));
              }).catch((err)=>{
                console.error(err);
              });
          }
        }
        return[OpenApiDocPage];
      })();
    const[]=(function(){
        return[];
      })();
    return{FileSystemPage,LandingPage,LibraryPage,LoginPage,NoteContentPage,NoteContext,
          NoteEditPage,NotesPage,OpenApiDocPage,PlaylistPage,PublicFilePage,SavedSearchPage,
          SettingsPage,StoragePage,StoragePreviewPage,SyncPage,UserRadioPage,fmtEpochTime};
    
  })(api,audio,components,daedalus,resources,router,store);
app=(function(api,components,daedalus,pages,resources,router,store){
    "use strict";
    const StyleSheet=daedalus.StyleSheet;
    const DomElement=daedalus.DomElement;
    const ButtonElement=daedalus.ButtonElement;
    const TextElement=daedalus.TextElement;
    const TextInputElement=daedalus.TextInputElement;
    const AuthenticatedRouter=daedalus.AuthenticatedRouter;
    const style={body:'dcs-1e053eca-0',rootWebDesktop:'dcs-1e053eca-1',rootWebMobile:'dcs-1e053eca-2',
          rootMobile:'dcs-1e053eca-3',margin:'dcs-1e053eca-4',fullsize:'dcs-1e053eca-5',
          show:'dcs-1e053eca-6',hide:'dcs-1e053eca-7'};
    function buildRouter(parent,container){
      const u=router.route_urls;
      let rt=new router.AppRouter(container);
      rt.addAuthRoute(u.userStoragePreview,(cbk)=>parent.handleRoute(cbk,pages.StoragePreviewPage),
              '/login');
      rt.addAuthRoute(u.userStorage,(cbk)=>parent.handleRoute(cbk,pages.StoragePage),
              '/login');
      rt.addAuthRoute(u.userFs,(cbk)=>parent.handleRoute(cbk,pages.FileSystemPage),
              '/login');
      rt.addAuthRoute(u.userPlaylist,(cbk)=>parent.handleRoute(cbk,pages.PlaylistPage),
              '/login');
      rt.addAuthRoute(u.userSettings,(cbk)=>parent.handleRoute(cbk,pages.SettingsPage),
              '/login');
      rt.addAuthRoute(u.userNotesEdit,(cbk)=>parent.handleRoute(cbk,pages.NoteEditPage),
              '/login');
      rt.addAuthRoute(u.userNotesContent,(cbk)=>parent.handleRoute(cbk,pages.NoteContentPage),
              '/login');
      rt.addAuthRoute(u.userNotesList,(cbk)=>parent.handleRoute(cbk,pages.NotesPage),
              '/login');
      rt.addAuthRoute(u.userLibraryList,(cbk)=>parent.handleRoute(cbk,pages.LibraryPage),
              '/login');
      rt.addAuthRoute(u.userLibrarySync,(cbk)=>parent.handleRoute(cbk,pages.SyncPage),
              '/login');
      rt.addAuthRoute(u.userLibrarySavedSearch,(cbk)=>parent.handleRoute(cbk,pages.SavedSearchPage),
              '/login');
      rt.addAuthRoute(u.userRadio,(cbk)=>parent.handleRoute(cbk,pages.UserRadioPage),
              '/login');
      rt.addAuthRoute(u.userWildCard,(cbk)=>{
          history.pushState({},"","/u/storage/list");
        },'/login');
      rt.addNoAuthRoute(u.login,(cbk)=>parent.handleRoute(cbk,pages.LoginPage),"/u/library/list");
      
      rt.addAuthRoute(u.apiDoc,(cbk)=>parent.handleRoute(cbk,pages.OpenApiDocPage),
              "/login");
      rt.addRoute(u.publicFile,(cbk)=>{
          parent.handleRoute(cbk,pages.PublicFilePage);
        });
      rt.addRoute(u.wildCard,(cbk)=>{
          parent.handleRoute(cbk,pages.LandingPage);
        });
      rt.setDefaultRoute((cbk)=>{
          parent.handleRoute(cbk,pages.LandingPage);
        });
      return rt;
    }
    class Root extends DomElement {
      constructor(){
        super("div",{},[]);
        const body=document.getElementsByTagName("BODY")[0];
        body.className=style.body;
        this.attrs={main:new pages.LandingPage,page_cache:{},nav:null,router:null,
                  container:new DomElement("div",{},[])};
        window.onresize=this.handleResize.bind(this);
      }
      doNavigate(res_path){
        if(!this.attrs.nav.isFixed()){
          setTimeout(()=>{
              history.pushState({},"",res_path);
              window.scrollTo(0,0);
              console.log("scrolled");
            },500);
        }else{
          history.pushState({},"",res_path);
          window.scrollTo(0,0);
          console.log("scrolled");
        }
        this.attrs.nav.hide();
      }
      buildRouter(){
        this.attrs.router=buildRouter(this,this.attrs.container);
        this.attrs.nav=new components.NavMenu();
        store.globals.showMenu=()=>{
          this.attrs.nav.show();
        };
        this.attrs.nav.addAction(resources.svg.music_note,"Playlist",()=>{
            this.doNavigate("/u/playlist");
          });
        this.attrs.nav.addAction(resources.svg.playlist,"Library",()=>{
            this.doNavigate("/u/library/list");
          });
        this.attrs.nav.addSubAction(resources.svg.bolt,"Dynamic Playlist",()=>{
            this.doNavigate("/u/library/saved");
          });
        if(daedalus.platform.isAndroid){
          this.attrs.nav.addSubAction(resources.svg.download,"Sync",()=>{
              this.doNavigate("/u/library/sync");
            });
        }
        this.attrs.nav.addAction(resources.svg.documents,"Storage",()=>{
            this.doNavigate("/u/storage/list");
          });
        this.attrs.nav.addSubAction(resources.svg.note,"Notes",()=>{
            this.doNavigate("/u/notes");
          });
        if(daedalus.platform.isAndroid){
          this.attrs.nav.addAction(resources.svg.documents,"File System",()=>{
              this.doNavigate("/u/fs");
            });
        }
        this.attrs.nav.addAction(resources.svg.settings,"Settings",()=>{
            this.doNavigate("/u/settings");
          });
        this.attrs.nav.addAction(resources.svg.logout,"Log Out",()=>{
            api.clearUserToken();
            this.attrs.page_cache={};
            history.pushState({},"","/");
          });
        this.toggleShowMenuFixed();
        this.appendChild(this.attrs.container);
        this.appendChild(this.attrs.nav);
        this.handleLocationChanged();
        this.connect(history.locationChanged,this.handleLocationChanged.bind(this));
        
      }
      handleLocationChanged(){
        this.toggleShowMenuFixed();
        this.attrs.router.handleLocationChanged(window.location.pathname);
      }
      handleRoute(fn,page){
        if(this.attrs.page_cache[page]===undefined){
          this.attrs.page_cache[page]=new page();
        }
        fn(this.attrs.page_cache[page]);
      }
      elementMounted(){
        this.updateMargin();
        const token=api.getUsertoken();
        if(!!token){
          api.validate_token(token).then((data)=>{
              if(!data.token_is_valid){
                api.clearUserToken();
              }
              this.buildRouter();
            }).catch((err)=>{
              console.error(err);
              if(daedalus.platform.isAndroid){
                this.buildRouter();
              }
            });
        }else{
          this.buildRouter();
        }
      }
      handleResize(event){
        this.toggleShowMenuFixed();
      }
      toggleShowMenuFixed(){
        if(!this.attrs.nav){
          return;
        }
        let condition=(document.body.clientWidth>900)&&(!!api.getUsertoken());
        if(!location.pathname.startsWith("/u")||location.pathname.startsWith("/u/storage/preview")){
        
          this.attrs.nav.addClassName(style.hide);
          this.attrs.nav.removeClassName(style.show);
          condition=false;
        }else{
          this.attrs.nav.addClassName(style.show);
          this.attrs.nav.removeClassName(style.hide);
        }
        this.attrs.nav.showFixed(condition);
        if(!!this.attrs.container){
          if(!!condition){
            this.attrs.container.addClassName(style.fullsize);
          }else{
            this.attrs.container.removeClassName(style.fullsize);
          }
        }
      }
      updateMargin(){
        if(daedalus.platform.isMobile){
          this.addClassName(style.rootWebMobile);
        }else{
          this.addClassName(style.rootWebDesktop);
        }
      }
    }
    return{Root};
  })(api,components,daedalus,pages,resources,router,store);
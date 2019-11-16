'use strict'
//app.js
//npm install body-parse
//npm install mongoose
const fs=require('fs')
const express=require('express')
const http=require('http')
var mongoose=require('mongoose');
const multer=require('multer')
let upload = multer({ dest: 'uploads/' })
var bodyParser = require('body-parser')

let app=express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}));
const FILE_PATH="public/images/"
const Ima="images/"
app.use(express.static('public'))
//connect databse
mongoose.set('useCreateIndex', true);
mongoose.connect('mongodb://localhost:27017/test',{useNewUrlParser:true,useUnifiedTopology:true});
var db=mongoose.connection;
//连接成功
db.on('open', function(){
	console.log('MongoDB Connection Successed');
});
// 连接失败
db.on('error', function(){
	console.log('MongoDB Connection Error');
});

// 声明一个数据集 对象
var userSchema =mongoose.Schema({
	username: {
		type: String,
		unique: true
	},
	password: {
		type: String
	},
	path: {
		type: String,
		default:null
	}
});
var User = mongoose.model('User',userSchema);
var friendSchema = mongoose.Schema({
	username:{
		type:String
	},
	friend: {
		type:String
	}
});
var Friend = mongoose.model('Friend',friendSchema);

// var Pic=mongoose.model('Pic',picSchema);
// var pic=new Pic({path:'FILE_PATH+req.file.originalname'});
// pic.save(function(err,pic){
// 	if(err) return console.error(err);
// 	console.log("stored a pic");

app.get('/up', upload.single(''), function (req, res) {
 	res.render('up');
 });

app.get('/upload', upload.single(''), function (req, res) {
	res.render('upload');
});


app.get('/search', upload.single(''), function (req, res) {
	res.render('search');
});

app.get('/follow', upload.single(''), function (req, res) {
	res.render('follow');
});
app.get('/friendlist', upload.single(''), function (req, res) {
	res.render('friendlist');
});
//  登录和注册
app.post('/up',upload.single(''), function (req, res) {
	var postData = {
		username: req.body.username,
		password: req.body.password
	};
	if(req.body.type==1){
		User.findOne({
			username: postData.username,
			password: postData.password
		}, function (err, data) {
			if(err) throw err;
			if(data){
				console.log("data" + data.path);
				res.send(data.path)
			} else{
				//根据用户名来搜索path
				// 	var wherestr = {'username' :postData.username};
				// 	var opt = {"path": 1};
				// 	User.find(wherestr,opt, function(err, data){
				// 		if (err) {
				// 			console.log("Error:" + err);
				// 		} else {
				res.send("0");
				console.log("login err");
			}
		} )
	}
	else{
		// 查询是否被注册
		User.findOne({username: postData.username}, function (err, data) {
			if (err) throw err;
			if (data) {
				console.log('register err');
				res.send('0');
			} else {
				// 保存到数据库
				User.create(postData, function (err, data) {
					if (err) throw err;
					console.log('register successful');
					res.send('1');
					//res.redirect('/userList');      // 重定向到所用用户列表
				})
			}
		});
	}
});

app.post('/search',upload.single(''), function (req, res) {
	//console.log(req.body.username);
	User.findOne({
		username: req.body.username
	}, function (err, data) {
		if(err) throw err;	
		if(data){
			res.send(data.path);
			console.log("Res" + data.path);
			//console.log('search err');
			//res.send('0')
		} else{
				//根据用户名来搜索path
				// 	var wherestr = {'username' :postData.username};
				// 	var opt = {"path": 1};
				// 	User.find(wherestr,opt, function(err, data){
				// 		if (err) {
				// 			console.log("Error:" + err);
				
			console.log("search err");
			res.send('0');
			//res.send(data.path);
			
			//console.log("Res:" + data.path);
		}
	} )
});




//注册传照片
app.post('/upload', upload.single('image'), function (req, res, next) {
	var t = Date.now();
	let output=fs.createWriteStream(FILE_PATH+ t +".jpg")
	let input=fs.createReadStream(req.file.path)
	input.pipe(output);
    var Url = "http://182.92.165.130:3000/" + Ima + t +".jpg";

	function update(){
		var wherestr = {'username' :req.body.username};
		var updatestr = {'path':Url};
		User.update(wherestr, updatestr, function(err, data){
			if (err) throw err;
			if(data){
				console.log('received a pic');
				res.send('1');
			}else{
				console.log('received err');
				res.send('0');
			}
		})
	}
	update();
})

//关注和取消关注
app.post('/follow',upload.single(''), function (req, res) {
	var postData = {
		username: req.body.username,
		friend:req.body.friend
	};
	if(req.body.type==1){
		function del() {
			// 取消关注
			var wherestr = {username:postData.username,friend:postData.friend};
			Friend.remove(wherestr, function(err, data) {
				if(err) throw err;
				if (data) {
					console.log("unfollow successful");
					res.send('1');
				} else {
					console.log("unfollow err");
					res.send('0');
				}
			});
		}
		del();
	} else {
		// 关注
		Friend.create(postData, function (err, data) {
			if (err) throw err;
			if(data) {
				console.log('follow successful');
				res.send('1');
			}else{
				console.log('follow err');
				res.send('0');
			}
		})
	}
});

//返回好友列表
app.post('/friendlist',upload.single(''), function (req, res) {
	console.log(req.body.username);
	var str = "";
	Friend.find({
		username: req.body.username
	}, function (err, data) {
		if(err) throw err;
		if(data){
			
			//data.forEach(v=>str + v.friend + ","),
			//str=str.substr(0,str.length -1),
			//res.send(str),
			//console.log("friend:" + str)
			
			var promise = new Promise(function (resolve, reject) {
				//两个参数： resolve 成功的回调函数名  ， reject失败的回调函数名
				data.forEach(v=>str += v.friend + ","),
					str.substr(0,str.length -1),
					//console.log("entrance-promise");	
					resolve(str);
				})
			promise.then(function (str) {
				//then第一个函数是成功的回调，参数是resolve(err)中的data
				res.send(str),
					console.log("friend:" + str);
			});


		}else{
			console.log('no friend');
			res.send('0');
		}
	} )
});




let server=http.createServer(app,function(req,res){
	try{
		res.setHeader("Content_Type","application/son;charset=utf-8");
	}catch(e){
		console.log(e);
	}
	res.end();
});

server.listen(3000,function () {
	console.log("start server at port 3000");
});
module.exports=app;


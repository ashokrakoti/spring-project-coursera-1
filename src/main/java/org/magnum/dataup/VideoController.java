/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.Multipart;



@RestController
public class VideoController {

    //crerating an emptylist of video objects.
	List<Video> videos = new ArrayList<>();

	//crerting a constant id
	private static final AtomicLong id = new AtomicLong(0L);

	//injecting VideofileManager refernce
	@Autowired
	private VideoFileManager videoFileManager;


   // a constructor
	public VideoController() throws IOException {
	}
    
	//method returns a videoUrl by taking an Id as input.
	private String getDataUrl(long videoId){
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	//this also returns a url from id input.
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base =
				"http://"+request.getServerName()
						+ ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		return base;
	}

	//get video handler
	@GetMapping("/video")
	public ResponseEntity<List<Video>> GetVideo(){
		return new ResponseEntity<>(videos, HttpStatus.OK);
	}

	//create new video meta data handler
	@PostMapping("/video")
	public ResponseEntity<Video> PostVideo(@RequestBody Video v){
		v.setId(id.incrementAndGet());
		v.setDataUrl(getDataUrl(v.getId()));
		videos.add(v);
		return new ResponseEntity<>(v, HttpStatus.OK);
	}

	
	//particular video data  with id returning handler
	@PostMapping("/video/{id}/data")
	@Multipart
	public ResponseEntity<VideoStatus> PostVideoData(@PathVariable("id") long id, @RequestParam("data")
	MultipartFile videoData ) throws IOException {
		String url = null;
		Video video = null;
		for(Video v: videos){
			if(v.getId() == id){
				url = v.getDataUrl();
				video = v;
			}
		}
		if(url == null){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		else{
			videoFileManager = VideoFileManager.get();
			videoFileManager.saveVideoData(video, videoData.getInputStream());
			return new ResponseEntity<>(new VideoStatus(VideoStatus.VideoState.READY), HttpStatus.OK);
		}
	}

	//getting video data using id handler
	@GetMapping("video/{id}/data")
	@Multipart
	public ResponseEntity<Video> GetVideoData(@PathVariable("id")long id, HttpServletResponse response) throws IOException {
		Video video = null;
		for(Video v: videos){
			if(v.getId() == id){
				video = v;
			}
		}
		if(video == null){
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		else{
			videoFileManager = VideoFileManager.get();
			videoFileManager.copyVideoData(video, response.getOutputStream());
			return new ResponseEntity<>(null, HttpStatus.OK);
		}
	}

}





@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)

public @ResponseBody VideoStatus setVideoData(@RequestParam("data")
MultipartFile data, @PathVariable("id") Long id, HttpServletResponse
response) throws IOException {
}
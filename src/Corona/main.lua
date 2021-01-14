local platform = system.getInfo("platform")

local txt
local source
local function logOnScreen(...)
	local args = {...}
	for i=1, #args do
	    args[i] = tostring(args[i])
	end
	local text = table.concat(args, " ")
	if txt == nil then
		txt = display.newText(text, display.contentCenterX, 50, native.systemFont, 10)
	else
		txt.text = text
		txt.isVisible = true
	end
	txt:setFillColor(1, 0, 1, 1)
	-- _D(...)
	if source then
		timer.cancel(source)
	end
	source = timer.performWithDelay(4000, function()
		-- txt:removeSelf()
		txt.isVisible = false
	end)
end

local function write( path, data, baseDir )

    path = system.pathForFile( path, baseDir or system.DocumentsDirectory )

    local file = io.open( path, "wb" )

    if file then
        file:write( data )
        io.close( file )
        file = nil
    end

end


if platform == 'android' then
	local Assets = require "plugin.android.assets"

	local ok, content = Assets.read("Icon.png")
	-- local ok, content = Assets.read("test.json")
	print(ok, type(content))
	-- logOnScreen(content, "assets.read")
	local result = Assets.doesAssetFileExist("test.json")
	print(result)
	result = Assets.doesAssetFileExist("ddsa.png")
	print(result)
	-- logOnScreen(result)

	if ok then
		write("test_copy_icon1.png", content, system.DocumentsDirectory)
		local obj = display.newImage("test_copy_icon1.png", system.DocumentsDirectory)
		obj:translate(display.contentCenterX, display.contentCenterY)
	end

	ok, content = Assets.read("empty_file.txt")
	local f1 = Assets.exists("empty_file.txt")
	local f2 = Assets.exists("nnn.txt")
	logOnScreen("empty", ok, content, type(content), f1, f2)

	local f3 = Assets.getReader("test.json")
	while f3:ready() do
		print(f3:readLine())
	end
	-- if f1 then
	-- 	f1:close()
	-- end
	-- if f2 then
	-- 	f2:close()
	-- end
	-- local t = assets.test()
	-- print(type(t))
	-- if type(t) == "table" then
	-- 	for k,v in pairs(t) do
	-- 		print(k, v)
	-- 	end
	-- end
	-- print(t.copyFile)
	-- local inputStream = t:openFile("Icon.png")
	-- print(type(inputStream))

	-- print(inputStream:available())
	-- local bytes = {}
	-- local n = 1
	-- local byte = inputStream:read()
	-- local char = string.char
	-- while byte ~= -1 do
	-- 	bytes[n] = char(byte)
	-- 	byte = inputStream:read()
	-- 	n = n + 1
	-- end
	-- local data = table.concat(bytes)
	-- write("test_copy_icon2.png", data, system.DocumentsDirectory)
	-- local obj = display.newImage("test_copy_icon2.png", system.DocumentsDirectory)
	-- obj:translate(display.contentCenterX, display.contentCenterY)
	-- print(data)
end

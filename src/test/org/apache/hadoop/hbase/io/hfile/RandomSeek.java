begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|LocalFileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|RawLocalFileSystem
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Random seek test.  */
end_comment

begin_class
specifier|public
class|class
name|RandomSeek
block|{
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|slurp
parameter_list|(
name|String
name|fname
parameter_list|)
throws|throws
name|IOException
block|{
name|BufferedReader
name|istream
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|fname
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|str
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|str
operator|=
name|istream
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|parts
init|=
name|str
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|parts
index|[
literal|0
index|]
operator|+
literal|":"
operator|+
name|parts
index|[
literal|1
index|]
operator|+
literal|":"
operator|+
name|parts
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
name|istream
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|l
return|;
block|}
specifier|private
specifier|static
name|String
name|randKey
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|keys
parameter_list|)
block|{
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|//return keys.get(r.nextInt(keys.size()));
return|return
literal|"2"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
literal|7
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|r
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
argument_list|)
return|;
comment|//return new String(r.nextInt(100));
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|argv
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
literal|64
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|RawLocalFileSystem
name|rlfs
init|=
operator|new
name|RawLocalFileSystem
argument_list|()
decl_stmt|;
name|rlfs
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LocalFileSystem
name|lfs
init|=
operator|new
name|LocalFileSystem
argument_list|(
name|rlfs
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"/Users/ryan/rfile.big.txt"
argument_list|)
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|SimpleBlockCache
name|cache
init|=
operator|new
name|SimpleBlockCache
argument_list|()
decl_stmt|;
comment|//LruBlockCache cache = new LruBlockCache();
name|Reader
name|reader
init|=
operator|new
name|HFile
operator|.
name|Reader
argument_list|(
name|lfs
argument_list|,
name|path
argument_list|,
name|cache
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|reader
operator|.
name|trailer
argument_list|)
expr_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Index read time: "
operator|+
operator|(
name|end
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|keys
init|=
name|slurp
argument_list|(
literal|"/Users/ryan/xaa.50k"
argument_list|)
decl_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|()
decl_stmt|;
name|int
name|count
decl_stmt|;
name|long
name|totalBytes
init|=
literal|0
decl_stmt|;
name|int
name|notFound
init|=
literal|0
decl_stmt|;
name|start
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
for|for
control|(
name|count
operator|=
literal|0
init|;
name|count
operator|<
literal|500000
condition|;
operator|++
name|count
control|)
block|{
name|String
name|key
init|=
name|randKey
argument_list|(
name|keys
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bkey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|res
init|=
name|scanner
operator|.
name|seekTo
argument_list|(
name|bkey
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|0
condition|)
block|{
name|ByteBuffer
name|k
init|=
name|scanner
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ByteBuffer
name|v
init|=
name|scanner
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|totalBytes
operator|+=
name|k
operator|.
name|limit
argument_list|()
expr_stmt|;
name|totalBytes
operator|+=
name|v
operator|.
name|limit
argument_list|()
expr_stmt|;
block|}
else|else
block|{
operator|++
name|notFound
expr_stmt|;
block|}
if|if
condition|(
name|res
operator|==
operator|-
literal|1
condition|)
block|{
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
block|}
comment|// Scan for another 1000 rows.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|scanner
operator|.
name|next
argument_list|()
condition|)
break|break;
name|ByteBuffer
name|k
init|=
name|scanner
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ByteBuffer
name|v
init|=
name|scanner
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|totalBytes
operator|+=
name|k
operator|.
name|limit
argument_list|()
expr_stmt|;
name|totalBytes
operator|+=
name|v
operator|.
name|limit
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|count
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|end
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Cache block count: "
operator|+
name|cache
operator|.
name|size
argument_list|()
operator|+
literal|" dumped: "
operator|+
name|cache
operator|.
name|dumps
argument_list|)
expr_stmt|;
comment|//System.out.println("Cache size: " + cache.heapSize());
name|double
name|msTime
init|=
operator|(
operator|(
name|end
operator|-
name|start
operator|)
operator|/
literal|1000000.0
operator|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Seeked: "
operator|+
name|count
operator|+
literal|" in "
operator|+
name|msTime
operator|+
literal|" (ms) "
operator|+
operator|(
literal|1000.0
operator|/
name|msTime
operator|)
operator|+
literal|" seeks/ms "
operator|+
operator|(
name|msTime
operator|/
literal|1000.0
operator|)
operator|+
literal|" ms/seek"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Total bytes: "
operator|+
name|totalBytes
operator|+
literal|" not found: "
operator|+
name|notFound
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|FileSystem
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
name|hbase
operator|.
name|HBaseTestingUtility
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestHFileReaderV1
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|N
init|=
literal|1000
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadingExistingVersion1HFile
parameter_list|()
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
name|TestHFileReaderV1
operator|.
name|class
operator|.
name|getResource
argument_list|(
literal|"8e8ab58dcf39412da19833fcd8f687ac"
argument_list|)
decl_stmt|;
name|Path
name|existingHFilePath
init|=
operator|new
name|Path
argument_list|(
name|url
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|existingHFilePath
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|FixedFileTrailer
name|trailer
init|=
name|reader
operator|.
name|getTrailer
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|N
argument_list|,
name|reader
operator|.
name|getEntries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|N
argument_list|,
name|trailer
operator|.
name|getEntryCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|trailer
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
argument_list|,
name|trailer
operator|.
name|getCompressionCodec
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|boolean
name|pread
range|:
operator|new
name|boolean
index|[]
block|{
literal|false
block|,
literal|true
block|}
control|)
block|{
name|int
name|totalDataSize
init|=
literal|0
decl_stmt|;
name|int
name|n
init|=
literal|0
decl_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
name|pread
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|scanner
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
do|do
block|{
name|totalDataSize
operator|+=
name|scanner
operator|.
name|getKey
argument_list|()
operator|.
name|limit
argument_list|()
operator|+
name|scanner
operator|.
name|getValue
argument_list|()
operator|.
name|limit
argument_list|()
operator|+
name|Bytes
operator|.
name|SIZEOF_INT
operator|*
literal|2
expr_stmt|;
operator|++
name|n
expr_stmt|;
block|}
do|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
do|;
comment|// Add magic record sizes, one per data block.
name|totalDataSize
operator|+=
literal|8
operator|*
name|trailer
operator|.
name|getDataIndexCount
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|N
argument_list|,
name|n
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|trailer
operator|.
name|getTotalUncompressedBytes
argument_list|()
argument_list|,
name|totalDataSize
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


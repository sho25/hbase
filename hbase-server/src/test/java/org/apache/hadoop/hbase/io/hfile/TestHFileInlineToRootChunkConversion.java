begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|CellUtil
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|SmallTests
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Test a case when an inline index chunk is converted to a root one. This reproduces the bug in  * HBASE-6871. We write a carefully selected number of relatively large keys so that we accumulate  * a leaf index chunk that only goes over the configured index chunk size after adding the last  * key/value. The bug is in that when we close the file, we convert that inline (leaf-level) chunk  * into a root chunk, but then look at the size of that root chunk, find that it is greater than  * the configured chunk size, and split it into a number of intermediate index blocks that should  * really be leaf-level blocks. If more keys were added, we would flush the leaf-level block, add  * another entry to the root-level block, and that would prevent us from upgrading the leaf-level  * chunk to the root chunk, thus not triggering the bug.   */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHFileInlineToRootChunkConversion
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
init|=
name|testUtil
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testWriteHFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|hfPath
init|=
operator|new
name|Path
argument_list|(
name|testUtil
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TestHFileInlineToRootChunkConversion
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".hfile"
argument_list|)
decl_stmt|;
name|int
name|maxChunkSize
init|=
literal|1024
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HFileBlockIndex
operator|.
name|MAX_CHUNK_SIZE_KEY
argument_list|,
name|maxChunkSize
argument_list|)
expr_stmt|;
name|HFileContext
name|context
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|16
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFileWriterV2
name|hfw
init|=
operator|(
name|HFileWriterV2
operator|)
operator|new
name|HFileWriterV2
operator|.
name|WriterFactoryV2
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|hfPath
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"key"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%05d"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|100
condition|;
operator|++
name|j
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'0'
operator|+
name|j
argument_list|)
expr_stmt|;
block|}
name|String
name|keyStr
init|=
name|sb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|byte
index|[]
name|k
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keyStr
argument_list|)
decl_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
operator|+
name|i
argument_list|)
decl_stmt|;
name|hfw
operator|.
name|append
argument_list|(
name|CellUtil
operator|.
name|createCell
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hfw
operator|.
name|close
argument_list|()
expr_stmt|;
name|HFileReaderV2
name|reader
init|=
operator|(
name|HFileReaderV2
operator|)
name|HFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|hfPath
argument_list|,
name|cacheConf
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Scanner doesn't do Cells yet.  Fix.
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|scanner
operator|.
name|seekTo
argument_list|(
name|CellUtil
operator|.
name|createCell
argument_list|(
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
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


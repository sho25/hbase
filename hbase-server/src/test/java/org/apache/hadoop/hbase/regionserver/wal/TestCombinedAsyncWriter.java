begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
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
name|HBaseClassTestRule
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
name|TableName
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
name|MediumTests
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
name|RegionServerTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|AsyncFSWALProvider
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
name|wal
operator|.
name|AsyncFSWALProvider
operator|.
name|AsyncWriter
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
name|wal
operator|.
name|WALFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioSocketChannel
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCombinedAsyncWriter
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestCombinedAsyncWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|EventLoopGroup
name|EVENT_LOOP_GROUP
decl_stmt|;
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|CHANNEL_CLASS
decl_stmt|;
specifier|private
specifier|static
name|WALFactory
name|WALS
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|EVENT_LOOP_GROUP
operator|=
operator|new
name|NioEventLoopGroup
argument_list|()
expr_stmt|;
name|CHANNEL_CLASS
operator|=
name|NioSocketChannel
operator|.
name|class
expr_stmt|;
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|)
expr_stmt|;
name|WALS
operator|=
operator|new
name|WALFactory
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TestCombinedAsyncWriter
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|WALS
operator|!=
literal|null
condition|)
block|{
name|WALS
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|EVENT_LOOP_GROUP
operator|.
name|shutdownGracefully
argument_list|()
operator|.
name|syncUninterruptibly
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithTrailer
parameter_list|()
throws|throws
name|IOException
block|{
name|doTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithoutTrailer
parameter_list|()
throws|throws
name|IOException
block|{
name|doTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|getPath
parameter_list|(
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|methodName
init|=
name|name
operator|.
name|getMethodName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[^A-Za-z0-9_-]"
argument_list|,
literal|"_"
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|()
argument_list|,
name|methodName
operator|+
literal|"-"
operator|+
name|index
argument_list|)
return|;
block|}
specifier|private
name|void
name|doTest
parameter_list|(
name|boolean
name|withTrailer
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|columnCount
init|=
literal|5
decl_stmt|;
name|int
name|recordCount
init|=
literal|5
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"tablename"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Path
name|path1
init|=
name|getPath
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Path
name|path2
init|=
name|getPath
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
try|try
init|(
name|AsyncWriter
name|writer1
init|=
name|AsyncFSWALProvider
operator|.
name|createAsyncWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path1
argument_list|,
literal|false
argument_list|,
name|EVENT_LOOP_GROUP
operator|.
name|next
argument_list|()
argument_list|,
name|CHANNEL_CLASS
argument_list|)
init|;
name|AsyncWriter
name|writer2
operator|=
name|AsyncFSWALProvider
operator|.
name|createAsyncWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|path2
argument_list|,
literal|false
argument_list|,
name|EVENT_LOOP_GROUP
operator|.
name|next
argument_list|()
argument_list|,
name|CHANNEL_CLASS
argument_list|)
init|;
name|CombinedAsyncWriter
name|writer
operator|=
name|CombinedAsyncWriter
operator|.
name|create
argument_list|(
name|writer1
argument_list|,
name|writer2
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doWrite
argument_list|(
operator|new
name|WriterOverAsyncWriter
argument_list|(
name|writer
argument_list|)
argument_list|,
name|withTrailer
argument_list|,
name|tableName
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|WALS
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path1
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
name|withTrailer
argument_list|,
name|tableName
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|ProtobufLogReader
name|reader
init|=
operator|(
name|ProtobufLogReader
operator|)
name|WALS
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path2
argument_list|)
init|)
block|{
name|ProtobufLogTestHelper
operator|.
name|doRead
argument_list|(
name|reader
argument_list|,
name|withTrailer
argument_list|,
name|tableName
argument_list|,
name|columnCount
argument_list|,
name|recordCount
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


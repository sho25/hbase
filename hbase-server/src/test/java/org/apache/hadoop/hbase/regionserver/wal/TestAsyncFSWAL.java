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
name|Threads
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

begin_comment
comment|/**  * Provides AsyncFSWAL test cases.  */
end_comment

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
name|TestAsyncFSWAL
extends|extends
name|AbstractTestFSWAL
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
name|TestAsyncFSWAL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|EventLoopGroup
name|GROUP
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
name|GROUP
operator|=
operator|new
name|NioEventLoopGroup
argument_list|(
literal|1
argument_list|,
name|Threads
operator|.
name|newDaemonThreadFactory
argument_list|(
literal|"TestAsyncFSWAL"
argument_list|)
argument_list|)
expr_stmt|;
name|CHANNEL_CLASS
operator|=
name|NioSocketChannel
operator|.
name|class
expr_stmt|;
name|AbstractTestFSWAL
operator|.
name|setUpBeforeClass
argument_list|()
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
name|AbstractTestFSWAL
operator|.
name|tearDownAfterClass
argument_list|()
expr_stmt|;
name|GROUP
operator|.
name|shutdownGracefully
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
name|newWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|String
name|archiveDir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|boolean
name|failIfWALExists
parameter_list|,
name|String
name|prefix
parameter_list|,
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
name|AsyncFSWAL
name|wal
init|=
operator|new
name|AsyncFSWAL
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|logDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|,
name|GROUP
argument_list|,
name|CHANNEL_CLASS
argument_list|)
decl_stmt|;
name|wal
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|wal
return|;
block|}
annotation|@
name|Override
specifier|protected
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
name|newSlowWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|String
name|archiveDir
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|boolean
name|failIfWALExists
parameter_list|,
name|String
name|prefix
parameter_list|,
name|String
name|suffix
parameter_list|,
specifier|final
name|Runnable
name|action
parameter_list|)
throws|throws
name|IOException
block|{
name|AsyncFSWAL
name|wal
init|=
operator|new
name|AsyncFSWAL
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|logDir
argument_list|,
name|archiveDir
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|failIfWALExists
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|,
name|GROUP
argument_list|,
name|CHANNEL_CLASS
argument_list|)
block|{
annotation|@
name|Override
name|void
name|atHeadOfRingBufferEventHandlerAppend
parameter_list|()
block|{
name|action
operator|.
name|run
argument_list|()
expr_stmt|;
name|super
operator|.
name|atHeadOfRingBufferEventHandlerAppend
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|wal
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|wal
return|;
block|}
block|}
end_class

end_unit


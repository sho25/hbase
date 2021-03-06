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
name|regionserver
operator|.
name|RegionServerServices
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
name|wal
operator|.
name|WALProvider
operator|.
name|Writer
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerServices
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
name|TestFSHLogDurability
extends|extends
name|WALDurabilityTestBase
argument_list|<
name|CustomFSHLog
argument_list|>
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
name|TestFSHLogDurability
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|CustomFSHLog
name|getWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|root
parameter_list|,
name|String
name|logDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|CustomFSHLog
name|wal
init|=
operator|new
name|CustomFSHLog
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logDir
argument_list|,
name|conf
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
name|void
name|resetSyncFlag
parameter_list|(
name|CustomFSHLog
name|wal
parameter_list|)
block|{
name|wal
operator|.
name|resetSyncFlag
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Boolean
name|getSyncFlag
parameter_list|(
name|CustomFSHLog
name|wal
parameter_list|)
block|{
return|return
name|wal
operator|.
name|getSyncFlag
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Boolean
name|getWriterSyncFlag
parameter_list|(
name|CustomFSHLog
name|wal
parameter_list|)
block|{
return|return
name|wal
operator|.
name|getWriterSyncFlag
argument_list|()
return|;
block|}
block|}
end_class

begin_class
class|class
name|CustomFSHLog
extends|extends
name|FSHLog
block|{
specifier|private
name|Boolean
name|syncFlag
decl_stmt|;
specifier|private
name|Boolean
name|writerSyncFlag
decl_stmt|;
specifier|public
name|CustomFSHLog
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|root
parameter_list|,
name|String
name|logDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Writer
name|createWriterInstance
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Writer
name|writer
init|=
name|super
operator|.
name|createWriterInstance
argument_list|(
name|path
argument_list|)
decl_stmt|;
return|return
operator|new
name|Writer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|writer
operator|.
name|getLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|boolean
name|forceSync
parameter_list|)
throws|throws
name|IOException
block|{
name|writerSyncFlag
operator|=
name|forceSync
expr_stmt|;
name|writer
operator|.
name|sync
argument_list|(
name|forceSync
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|boolean
name|forceSync
parameter_list|)
throws|throws
name|IOException
block|{
name|syncFlag
operator|=
name|forceSync
expr_stmt|;
name|super
operator|.
name|sync
argument_list|(
name|forceSync
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|long
name|txid
parameter_list|,
name|boolean
name|forceSync
parameter_list|)
throws|throws
name|IOException
block|{
name|syncFlag
operator|=
name|forceSync
expr_stmt|;
name|super
operator|.
name|sync
argument_list|(
name|txid
argument_list|,
name|forceSync
argument_list|)
expr_stmt|;
block|}
name|void
name|resetSyncFlag
parameter_list|()
block|{
name|this
operator|.
name|syncFlag
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|writerSyncFlag
operator|=
literal|null
expr_stmt|;
block|}
name|Boolean
name|getSyncFlag
parameter_list|()
block|{
return|return
name|syncFlag
return|;
block|}
name|Boolean
name|getWriterSyncFlag
parameter_list|()
block|{
return|return
name|writerSyncFlag
return|;
block|}
block|}
end_class

end_unit


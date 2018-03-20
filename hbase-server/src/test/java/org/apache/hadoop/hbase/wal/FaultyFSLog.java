begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|RegionInfo
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
name|wal
operator|.
name|FSHLog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|// imports for things that haven't moved yet
end_comment

begin_comment
comment|/**  * This is a utility class, used by tests, which fails operation specified by FailureType enum  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|FaultyFSLog
extends|extends
name|FSHLog
block|{
specifier|public
enum|enum
name|FailureType
block|{
name|NONE
block|,
name|APPEND
block|,
name|SYNC
block|}
name|FailureType
name|ft
init|=
name|FailureType
operator|.
name|NONE
decl_stmt|;
specifier|public
name|FaultyFSLog
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|String
name|logName
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
name|rootDir
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setFailureType
parameter_list|(
name|FailureType
name|fType
parameter_list|)
block|{
name|this
operator|.
name|ft
operator|=
name|fType
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
parameter_list|)
throws|throws
name|IOException
block|{
name|sync
argument_list|(
name|txid
argument_list|,
literal|false
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
if|if
condition|(
name|this
operator|.
name|ft
operator|==
name|FailureType
operator|.
name|SYNC
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"sync"
argument_list|)
throw|;
block|}
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
annotation|@
name|Override
specifier|public
name|long
name|append
parameter_list|(
name|RegionInfo
name|info
parameter_list|,
name|WALKeyImpl
name|key
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|boolean
name|inMemstore
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|ft
operator|==
name|FailureType
operator|.
name|APPEND
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"append"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|key
argument_list|,
name|edits
argument_list|,
name|inMemstore
argument_list|)
return|;
block|}
block|}
end_class

end_unit


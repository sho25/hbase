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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|WAL
operator|.
name|Entry
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
name|AsyncWriter
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
import|;
end_import

begin_class
class|class
name|WriterOverAsyncWriter
implements|implements
name|WALProvider
operator|.
name|Writer
block|{
specifier|private
specifier|final
name|WALProvider
operator|.
name|AsyncWriter
name|asyncWriter
decl_stmt|;
specifier|public
name|WriterOverAsyncWriter
parameter_list|(
name|AsyncWriter
name|asyncWriter
parameter_list|)
block|{
name|this
operator|.
name|asyncWriter
operator|=
name|asyncWriter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|asyncWriter
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
name|asyncWriter
operator|.
name|getLength
argument_list|()
return|;
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
name|asyncWriter
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
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
try|try
block|{
name|asyncWriter
operator|.
name|sync
argument_list|(
name|forceSync
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


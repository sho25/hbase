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
name|ipc
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|exceptions
operator|.
name|DoNotRetryIOException
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * An {@link RemoteException} with some extra information.  If source exception  * was a {@link DoNotRetryIOException}, {@link #isDoNotRetry()} will return true.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RemoteWithExtrasException
extends|extends
name|RemoteException
block|{
specifier|private
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doNotRetry
decl_stmt|;
specifier|public
name|RemoteWithExtrasException
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|msg
parameter_list|,
specifier|final
name|boolean
name|doNotRetry
parameter_list|)
block|{
name|this
argument_list|(
name|className
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
name|doNotRetry
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RemoteWithExtrasException
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|msg
parameter_list|,
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|boolean
name|doNotRetry
parameter_list|)
block|{
name|super
argument_list|(
name|className
argument_list|,
name|msg
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|doNotRetry
operator|=
name|doNotRetry
expr_stmt|;
block|}
comment|/**    * @return null if not set    */
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostname
return|;
block|}
comment|/**    * @return -1 if not set    */
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|port
return|;
block|}
comment|/**    * @return True if origin exception was a do not retry type.    */
specifier|public
name|boolean
name|isDoNotRetry
parameter_list|()
block|{
return|return
name|this
operator|.
name|doNotRetry
return|;
block|}
block|}
end_class

end_unit


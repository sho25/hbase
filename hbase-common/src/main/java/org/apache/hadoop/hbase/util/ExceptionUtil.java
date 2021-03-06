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
name|util
package|;
end_package

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
name|net
operator|.
name|SocketTimeoutException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|ClosedByInterruptException
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
comment|/**  * This class handles the different interruption classes.  * It can be:  * - InterruptedException  * - InterruptedIOException (inherits IOException); used in IO  * - ClosedByInterruptException (inherits IOException)  * - SocketTimeoutException inherits InterruptedIOException but is not a real  * interruption, so we have to distinguish the case. This pattern is unfortunately common.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ExceptionUtil
block|{
specifier|private
name|ExceptionUtil
parameter_list|()
block|{   }
comment|/**    * @return true if the throwable comes an interruption, false otherwise.    */
specifier|public
specifier|static
name|boolean
name|isInterrupt
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|InterruptedException
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|SocketTimeoutException
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
name|t
operator|instanceof
name|InterruptedIOException
operator|||
name|t
operator|instanceof
name|ClosedByInterruptException
operator|)
return|;
block|}
comment|/**    * @throws InterruptedIOException if t was an interruption. Does nothing otherwise.    */
specifier|public
specifier|static
name|void
name|rethrowIfInterrupt
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|InterruptedIOException
block|{
name|InterruptedIOException
name|iie
init|=
name|asInterrupt
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|iie
operator|!=
literal|null
condition|)
block|{
throw|throw
name|iie
throw|;
block|}
block|}
comment|/**    * @return an InterruptedIOException if t was an interruption, null otherwise    */
specifier|public
specifier|static
name|InterruptedIOException
name|asInterrupt
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|SocketTimeoutException
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|InterruptedIOException
condition|)
block|{
return|return
operator|(
name|InterruptedIOException
operator|)
name|t
return|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|InterruptedException
operator|||
name|t
operator|instanceof
name|ClosedByInterruptException
condition|)
block|{
name|InterruptedIOException
name|iie
init|=
operator|new
name|InterruptedIOException
argument_list|(
literal|"Origin: "
operator|+
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|iie
operator|.
name|initCause
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|iie
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


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
package|;
end_package

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
comment|/**  * Thrown if a region server is passed an unknown scanner ID.  * This usually means that the client has taken too long between checkins and so the  * scanner lease on the server-side has expired OR the server-side is closing  * down and has cancelled all leases.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|UnknownScannerException
extends|extends
name|DoNotRetryIOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|993179627856392526L
decl_stmt|;
specifier|public
name|UnknownScannerException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param message the message for this exception    */
specifier|public
name|UnknownScannerException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param message the message for this exception    * @param exception the exception to grab data from    */
specifier|public
name|UnknownScannerException
parameter_list|(
name|String
name|message
parameter_list|,
name|Exception
name|exception
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


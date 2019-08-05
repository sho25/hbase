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
comment|/**  * Failed to find {@code .tableinfo} file under the table directory.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|TableInfoMissingException
extends|extends
name|HBaseIOException
block|{
comment|/**    * Failed to find {@code .tableinfo} file under the table directory.    */
specifier|public
name|TableInfoMissingException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param message the message for this exception    */
specifier|public
name|TableInfoMissingException
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
comment|/**    * @param message the message for this exception    * @param throwable the {@link Throwable} to use for this exception    */
specifier|public
name|TableInfoMissingException
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|throwable
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param throwable the {@link Throwable} to use for this exception    */
specifier|public
name|TableInfoMissingException
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
name|super
argument_list|(
name|throwable
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


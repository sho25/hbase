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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
comment|/**  * CoprocessorDescriptor contains the details about how to build a coprocessor.  * This class is a pojo so there are no checks for the details carried by this class.  * Use {@link CoprocessorDescriptorBuilder} to instantiate a CoprocessorDescriptor  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|CoprocessorDescriptor
block|{
comment|/**    * @return the name of the class or interface represented by this object.    */
name|String
name|getClassName
parameter_list|()
function_decl|;
comment|/**    * @return Path of the jar file. If it's null, the class will be loaded from default classloader.    */
name|Optional
argument_list|<
name|String
argument_list|>
name|getJarPath
parameter_list|()
function_decl|;
comment|/**    * @return The order to execute this coprocessor    */
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/**    * @return Arbitrary key-value parameter pairs passed into the  coprocessor.    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProperties
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


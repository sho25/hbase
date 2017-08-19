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
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Certain JVM versions are known to be unstable with HBase. This  * class has a utility function to determine whether the current JVM  * is known to be unstable.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|JvmVersion
block|{
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|BAD_JVM_VERSIONS
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|BAD_JVM_VERSIONS
operator|.
name|add
argument_list|(
literal|"1.6.0_18"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return true if the current JVM is known to be unstable.    */
specifier|public
specifier|static
name|boolean
name|isBadJvmVersion
parameter_list|()
block|{
name|String
name|version
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.version"
argument_list|)
decl_stmt|;
return|return
name|version
operator|!=
literal|null
operator|&&
name|BAD_JVM_VERSIONS
operator|.
name|contains
argument_list|(
name|version
argument_list|)
return|;
block|}
block|}
end_class

end_unit


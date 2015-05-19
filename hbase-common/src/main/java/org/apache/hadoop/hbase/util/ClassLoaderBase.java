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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Base class loader that defines couple shared constants used by sub-classes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClassLoaderBase
extends|extends
name|URLClassLoader
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|DEFAULT_LOCAL_DIR
init|=
literal|"/tmp/hbase-local-dir"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|LOCAL_DIR_KEY
init|=
literal|"hbase.local.dir"
decl_stmt|;
comment|/**    * Parent class loader.    */
specifier|protected
specifier|final
name|ClassLoader
name|parent
decl_stmt|;
comment|/**    * Creates a DynamicClassLoader that can load classes dynamically    * from jar files under a specific folder.    *    * @param parent the parent ClassLoader to set.    */
specifier|public
name|ClassLoaderBase
parameter_list|(
specifier|final
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|URL
index|[]
block|{}
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|parent
argument_list|,
literal|"No parent classloader!"
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
block|}
block|}
end_class

end_unit


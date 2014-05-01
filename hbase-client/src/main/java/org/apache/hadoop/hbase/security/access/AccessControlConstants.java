begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|access
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|AccessControlConstants
block|{
comment|/**    * Configuration option that toggles whether EXEC permission checking is    * performed during coprocessor endpoint invocations.    */
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_PERMISSION_CHECKS_KEY
init|=
literal|"hbase.security.exec.permission.checks"
decl_stmt|;
comment|/** Default setting for hbase.security.exec.permission.checks; false */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_EXEC_PERMISSION_CHECKS
init|=
literal|false
decl_stmt|;
comment|/**    * Configuration or CF schema option for early termination of access checks    * if table or CF permissions grant access. Pre-0.98 compatible behavior    */
specifier|public
specifier|static
specifier|final
name|String
name|CF_ATTRIBUTE_EARLY_OUT
init|=
literal|"hbase.security.access.early_out"
decl_stmt|;
comment|/** Default setting for hbase.security.access.early_out */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_ATTRIBUTE_EARLY_OUT
init|=
literal|true
decl_stmt|;
comment|// Operation attributes for cell level security
comment|/** Cell level ACL */
specifier|public
specifier|static
specifier|final
name|String
name|OP_ATTRIBUTE_ACL
init|=
literal|"acl"
decl_stmt|;
comment|/** Cell level ACL evaluation strategy */
specifier|public
specifier|static
specifier|final
name|String
name|OP_ATTRIBUTE_ACL_STRATEGY
init|=
literal|"acl.strategy"
decl_stmt|;
comment|/** Default cell ACL evaluation strategy: Table and CF first, then ACL */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|OP_ATTRIBUTE_ACL_STRATEGY_DEFAULT
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|}
decl_stmt|;
comment|/** Alternate cell ACL evaluation strategy: Cell ACL first, then table and CF */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|OP_ATTRIBUTE_ACL_STRATEGY_CELL_FIRST
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|}
decl_stmt|;
block|}
end_interface

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|hbase
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Enumeration that represents the action HBase will take when a space quota is violated.  *  * The target for a violation policy is either an HBase table or namespace. In the case of a  * namespace, it is treated as a collection of tables (all tables are subject to the same policy).  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
enum|enum
name|SpaceViolationPolicy
block|{
comment|/**    * Disables the table(s).    */
name|DISABLE
block|,
comment|/**    * Disallows any mutations or compactions on the table(s).    */
name|NO_WRITES_COMPACTIONS
block|,
comment|/**    * Disallows any mutations (but allows compactions) on the table(s).    */
name|NO_WRITES
block|,
comment|/**    * Disallows any updates (but allows deletes and compactions) on the table(s).    */
name|NO_INSERTS
block|; }
end_enum

end_unit


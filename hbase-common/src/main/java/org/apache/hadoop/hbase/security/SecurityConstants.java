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
comment|/**  * SecurityConstants holds a bunch of kerberos-related constants  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SecurityConstants
block|{
comment|/**    * Configuration keys for programmatic JAAS configuration for secured master    * and regionserver interaction    */
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_KRB_PRINCIPAL
init|=
literal|"hbase.master.kerberos.principal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_KRB_KEYTAB_FILE
init|=
literal|"hbase.master.keytab.file"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_KRB_PRINCIPAL
init|=
literal|"hbase.regionserver.kerberos.principal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_KRB_KEYTAB_FILE
init|=
literal|"hbase.regionserver.keytab.file"
decl_stmt|;
specifier|private
name|SecurityConstants
parameter_list|()
block|{
comment|// Can't be instantiated with this ctor.
block|}
block|}
end_class

end_unit


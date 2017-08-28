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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|conf
operator|.
name|Configuration
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|security
operator|.
name|User
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
name|security
operator|.
name|access
operator|.
name|AccessController
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
name|testclassification
operator|.
name|IntegrationTests
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
name|util
operator|.
name|LoadTestTool
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
name|util
operator|.
name|test
operator|.
name|LoadTestDataGeneratorWithACL
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * /**  * An Integration class for tests that does something with the cluster while running  * {@link LoadTestTool} to write and verify some data.  * Verifies whether cells for users with only WRITE permissions are not read back  * and cells with READ permissions are read back.   * Every operation happens in the user's specific context  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|IntegrationTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|IntegrationTestIngestWithACL
extends|extends
name|IntegrationTestIngest
block|{
specifier|private
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|HYPHEN
init|=
literal|'-'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SPECIAL_PERM_CELL_INSERTION_FACTOR
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_SUPERUSER
init|=
literal|"superuser"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_USERS
init|=
literal|"userlist"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_AUTHN
init|=
literal|"authinfo"
decl_stmt|;
specifier|private
name|String
name|superUser
init|=
literal|"owner"
decl_stmt|;
specifier|private
name|String
name|userNames
init|=
literal|"user1,user2,user3,user4"
decl_stmt|;
specifier|private
name|String
name|authnFileName
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUpCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
name|getTestingUtil
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.master.classes"
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.coprocessor.region.classes"
argument_list|,
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.security.access.early_out"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// conf.set("hbase.superuser", "admin");
name|super
operator|.
name|setUpCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|getArgsForLoadTestTool
parameter_list|(
name|String
name|mode
parameter_list|,
name|String
name|modeSpecificArg
parameter_list|,
name|long
name|startKey
parameter_list|,
name|long
name|numKeys
parameter_list|)
block|{
name|String
index|[]
name|args
init|=
name|super
operator|.
name|getArgsForLoadTestTool
argument_list|(
name|mode
argument_list|,
name|modeSpecificArg
argument_list|,
name|startKey
argument_list|,
name|numKeys
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tmp
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
decl_stmt|;
name|tmp
operator|.
name|add
argument_list|(
name|HYPHEN
operator|+
name|LoadTestTool
operator|.
name|OPT_GENERATOR
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|LoadTestDataGeneratorWithACL
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
if|if
condition|(
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|authnFileName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|superUser
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|userNames
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|SPECIAL_PERM_CELL_INSERTION_FACTOR
argument_list|)
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tmp
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|tmp
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|super
operator|.
name|addOptions
argument_list|()
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|OPT_SUPERUSER
argument_list|,
literal|"Super user name used to add the ACL permissions"
argument_list|)
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|OPT_USERS
argument_list|,
literal|"List of users to be added with the ACLs.  Should be comma seperated."
argument_list|)
expr_stmt|;
name|super
operator|.
name|addOptWithArg
argument_list|(
name|OPT_AUTHN
argument_list|,
literal|"The name of the properties file that contains kerberos key tab file and principal definitions. "
operator|+
literal|"The principal key in the file should be of the form hbase.<username>.kerberos.principal."
operator|+
literal|" The keytab key in the file should be of the form hbase.<username>.keytab.file. Example:  "
operator|+
literal|"hbase.user1.kerberos.principal=user1/fully.qualified.domain.name@YOUR-REALM.COM, "
operator|+
literal|"hbase.user1.keytab.file=<filelocation>."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|super
operator|.
name|processOptions
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_SUPERUSER
argument_list|)
condition|)
block|{
name|superUser
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_SUPERUSER
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_USERS
argument_list|)
condition|)
block|{
name|userNames
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_USERS
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|boolean
name|authFileNotFound
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|OPT_AUTHN
argument_list|)
condition|)
block|{
name|authnFileName
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_AUTHN
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|authnFileName
argument_list|)
condition|)
block|{
name|authFileNotFound
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|authFileNotFound
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|authFileNotFound
condition|)
block|{
name|super
operator|.
name|printUsage
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|EXIT_FAILURE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|IntegrationTestingUtility
operator|.
name|setUseDistributedCluster
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|IntegrationTestIngestWithACL
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


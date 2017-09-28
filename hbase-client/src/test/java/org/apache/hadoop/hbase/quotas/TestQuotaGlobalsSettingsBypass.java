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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|TableName
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
name|quotas
operator|.
name|QuotaSettingsFactory
operator|.
name|QuotaGlobalsSettingsBypass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestQuotaGlobalsSettingsBypass
block|{
annotation|@
name|Test
specifier|public
name|void
name|testMerge
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaGlobalsSettingsBypass
name|orig
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|orig
operator|.
name|merge
argument_list|(
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidMerges
parameter_list|()
throws|throws
name|IOException
block|{
name|QuotaGlobalsSettingsBypass
name|userBypass
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|QuotaGlobalsSettingsBypass
name|tableBypass
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|null
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|QuotaGlobalsSettingsBypass
name|namespaceBypass
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"ns"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|QuotaGlobalsSettingsBypass
name|userOnTableBypass
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|QuotaGlobalsSettingsBypass
name|userOnNamespaceBypass
init|=
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
literal|null
argument_list|,
literal|"ns"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|userBypass
operator|.
name|merge
argument_list|(
name|userBypass
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"frank"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userBypass
argument_list|,
name|tableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userBypass
argument_list|,
name|namespaceBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userBypass
argument_list|,
name|userOnTableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userBypass
argument_list|,
name|userOnNamespaceBypass
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableBypass
operator|.
name|merge
argument_list|(
name|tableBypass
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|tableBypass
argument_list|,
name|userBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|tableBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|null
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|tableBypass
argument_list|,
name|namespaceBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|tableBypass
argument_list|,
name|userOnTableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|tableBypass
argument_list|,
name|userOnNamespaceBypass
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|namespaceBypass
operator|.
name|merge
argument_list|(
name|namespaceBypass
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|namespaceBypass
argument_list|,
name|userBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|namespaceBypass
argument_list|,
name|tableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|namespaceBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"sn"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|namespaceBypass
argument_list|,
name|userOnTableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|namespaceBypass
argument_list|,
name|userOnNamespaceBypass
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|userOnTableBypass
operator|.
name|merge
argument_list|(
name|userOnTableBypass
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
name|userBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
name|tableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
name|namespaceBypass
argument_list|)
expr_stmt|;
comment|// Incorrect user
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"frank"
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Incorrect tablename
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"bar"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnTableBypass
argument_list|,
name|userOnNamespaceBypass
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|userOnNamespaceBypass
operator|.
name|merge
argument_list|(
name|userOnNamespaceBypass
argument_list|)
operator|.
name|getBypass
argument_list|()
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
name|userBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
name|tableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
name|namespaceBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
name|userOnTableBypass
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"frank"
argument_list|,
literal|null
argument_list|,
literal|"ns"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|expectFailure
argument_list|(
name|userOnNamespaceBypass
argument_list|,
operator|new
name|QuotaGlobalsSettingsBypass
argument_list|(
literal|"joe"
argument_list|,
literal|null
argument_list|,
literal|"sn"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|expectFailure
parameter_list|(
name|QuotaSettings
name|one
parameter_list|,
name|QuotaSettings
name|two
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|one
operator|.
name|merge
argument_list|(
name|two
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected to see an Exception merging "
operator|+
name|two
operator|+
literal|" into "
operator|+
name|one
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{}
block|}
block|}
end_class

end_unit

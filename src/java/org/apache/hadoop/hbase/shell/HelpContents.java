begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|shell
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|HelpContents
block|{
comment|/**    * add help contents     */
specifier|public
specifier|static
name|Map
operator|<
condition|?
then|extends
name|String
operator|,
operator|?
expr|extends
name|String
index|[]
operator|>
name|Load
argument_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|load
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
block|;
name|load
operator|.
name|put
argument_list|(
literal|"SHOW"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"List all tables."
block|,
literal|"SHOW TABLES;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DESCRIBE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Describe a table's columnfamilies."
block|,
literal|"DESCRIBE<table_name>;"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"CREATE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Create a table"
block|,
literal|"CREATE<table_name>"
operator|+
literal|"\n\t  COLUMNFAMILIES('cf_name1'[, 'cf_name2', ...]);"
operator|+
literal|"\n    [LIMIT=versions_limit];"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DROP"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Drop columnfamilie(s) from a table or drop table(s)"
block|,
literal|"DROP table_name1[, table_name2, ...] | cf_name1[, cf_name2, ...];"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"INSERT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Insert row into table"
block|,
literal|"INSERT<table_name>"
operator|+
literal|"\n\t('column_name1'[, 'column_name2', ...])"
operator|+
literal|"\n\t    VALUES('entry1'[, 'entry2', ...])"
operator|+
literal|"\n    WHERE row='row_key';"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"DELETE"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Delete cell or row in table."
block|,
literal|"DELETE<table_name>"
operator|+
literal|"\n\t    WHERE row='row_key;"
operator|+
literal|"\n    [AND column='column_name'];"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"SELECT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Select values from a table"
block|,
literal|"SELECT<table_name>"
operator|+
literal|"\n\t    [WHERE row='row_key']"
operator|+
literal|"\n    [AND column='column_name'];"
operator|+
literal|"\n    [AND time='timestamp'];"
operator|+
literal|"\n    [LIMIT=versions_limit];"
block|}
argument_list|)
block|;
name|load
operator|.
name|put
argument_list|(
literal|"EXIT"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Exit shell"
block|,
literal|"EXIT;"
block|}
argument_list|)
block|;
return|return
name|load
return|;
block|}
block|}
end_class

end_unit


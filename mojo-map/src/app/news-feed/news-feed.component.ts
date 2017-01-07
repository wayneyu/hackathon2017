import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalDataSource } from 'ng2-smart-table';

import { NewsFeedItem } from './news-feed-item';
import { NewsFeedService } from './news-feed.service';
import { MockNewsFeedService } from './news-feed-mock.service';

@Component({
    selector: 'news-feed',
    templateUrl: './news-feed.component.html'
})
export class NewsFeedComponent implements OnInit, OnDestroy {

    private static readonly MAX_NEWS_ITEMS = 10;

    topic: string;
    newsItems: NewsFeedItem[];
	settings = {
		columns: {
			source: {
				title: 'Source'
			},
			timestamp: {
				title: 'Timestamp',
                sortDirection: 'desc'
			},
			summary: {
				title: 'Summary'
			},
			link: {
				title: 'Link',
                type: 'html'
			}
		},
		editable: false,
		actions: {
			add: false,
			edit: false,
			delete: false
		}
	};

	source: LocalDataSource;

    constructor(private newsFeedService: NewsFeedService) { }

    ngOnInit() {
        this.topic = 'Some topic';
        this.newsItems = [];
		this.newsItems = MockNewsFeedService.getFakeNewsItems();
  		this.source = new LocalDataSource(this.newsItems);
        // this.newsFeedService
        //     .subscribeToNews(this.topic)
        //     .subscribe(
        //         newsItem => {
        //             this.newsItems.push(newsItem.data);
        //         },
        //         error => {
        //             console.log('Error: ' + error.message); 
        //         },
        //         () => {
        //             console.log('Completed');
        //         }
        //     );
    }

    ngOnDestroy() {
        // this.newsFeedService.closeFeed();
    }
}